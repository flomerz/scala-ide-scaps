package scaps.eclipse.core.services

import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Status
import org.eclipse.core.runtime.SubMonitor
import org.eclipse.core.runtime.jobs.Job
import scaps.eclipse.core.adapters.ScapsAdapter
import scaps.eclipse.core.models.ResultList
import java.io.File
import org.eclipse.jdt.core.IPackageFragmentRoot
import org.eclipse.jdt.internal.core.PackageFragment
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.IJavaElement

object ScapsService {
  def apply(indexDir: String): ScapsService = {
    val scapsAdapter = new ScapsAdapter(indexDir)
    new ScapsService(scapsAdapter)
  }
}

class ScapsService(private val scapsAdapter: ScapsAdapter) {

  def index(classPath: Seq[String], projectSourceFragmentRoots: Seq[IPackageFragmentRoot], librarySourceRootFiles: Seq[File]) {

    def indexProjectTask(monitor: IProgressMonitor) {
      monitor.setTaskName("Index Project Sources")

      def findSourceFiles(fragmentRoot: IPackageFragmentRoot): Seq[ICompilationUnit] = {
        def recursiveFindSourceFiles(packageFragments: Array[IJavaElement]): Array[ICompilationUnit] = {
          // TODO: don't use instance cast
          val sourceFiles = packageFragments.flatMap(_.asInstanceOf[PackageFragment].getCompilationUnits)
          val subPackageFragments = packageFragments.flatMap(_.asInstanceOf[PackageFragment].getChildren)
          sourceFiles ++ recursiveFindSourceFiles(subPackageFragments)
        }
        recursiveFindSourceFiles(fragmentRoot.getChildren)
      }

      val projectSourceFilePaths = projectSourceFragmentRoots.flatMap(findSourceFiles)
      scapsAdapter.indexProject(classPath, projectSourceFilePaths)
    }

    def indexLibrariesTask(monitor: IProgressMonitor): Unit = {
      def indexLibraryTask(monitor: IProgressMonitor, librarySourceRootFile: File) {
        monitor.setTaskName(librarySourceRootFile.getName)
        scapsAdapter.indexLibrary(classPath, librarySourceRootFile)
      }

      monitor.setTaskName("Index Libraries")
      val subMonitor = SubMonitor.convert(monitor, librarySourceRootFiles.length)
      librarySourceRootFiles.foreach(indexLibraryTask(subMonitor, _))
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
    }.schedule
  }

}
