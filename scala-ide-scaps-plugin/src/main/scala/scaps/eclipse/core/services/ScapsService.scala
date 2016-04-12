package scaps.eclipse.core.services

import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Status
import org.eclipse.core.runtime.SubMonitor
import org.eclipse.core.runtime.jobs.Job
import scaps.eclipse.core.adapters.ScapsAdapter
import scaps.eclipse.core.models.ResultList
import java.io.File

object ScapsService {
  def apply(indexDir: String) {
    val scapsAdapter = new ScapsAdapter(indexDir)
    new ScapsService(scapsAdapter)
  }
}

class ScapsService(private val scapsAdapter: ScapsAdapter) {

  def index(classPath: Seq[String], projectSourcePath: String, librarySourcePaths: Seq[String]) {

    def indexProjectTask(monitor: IProgressMonitor) {
      monitor.setTaskName("Index Project Sources")

      def findSourceFiles(rootPath: String): List[String] = {

        def findFilesRecursive(rootFile: File): Array[File] = {
          val dirFiles = rootFile.listFiles
          dirFiles ++ dirFiles.filter(_.isDirectory).flatMap(findFilesRecursive)
        }

        findFilesRecursive(new File(rootPath)).map(_.getAbsolutePath).filter(!_.endsWith(".scala")).toList
      }

      val projectSourcePaths = findSourceFiles(projectSourcePath)
      scapsAdapter.indexProject(classPath, projectSourcePaths)
    }

    def indexLibrariesTask(monitor: IProgressMonitor) {

      def indexLibraryTask(monitor: IProgressMonitor, librarySourcePath: String) {
        monitor.setTaskName(librarySourcePath)
        scapsAdapter.indexLibrary(classPath, librarySourcePath)
      }

      monitor.setTaskName("Index Libraries")
      val subMonitor = SubMonitor.convert(monitor, librarySourcePaths.length)
      librarySourcePaths.foreach(indexLibraryTask(subMonitor, _))
    }

    def indexFinalize(monitor: IProgressMonitor) {
      monitor.setTaskName("Finalize Index")
      scapsAdapter.indexFinalize
    }

    new Job("Scaps Indexing") {
      def run(monitor: IProgressMonitor): IStatus = {
        val subMonitor = SubMonitor.convert(monitor, 3)
        indexProjectTask(subMonitor.newChild(1))
        indexLibrariesTask(subMonitor.newChild(1))
        indexFinalize(subMonitor.newChild(1))
        Status.OK_STATUS
      }
    }.schedule()
  }

}
