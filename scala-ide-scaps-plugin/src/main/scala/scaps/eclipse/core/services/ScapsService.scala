package scaps.eclipse.core.services

import java.io.File
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Status
import org.eclipse.core.runtime.SubMonitor
import org.eclipse.core.runtime.jobs.Job
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.IPackageFragmentRoot
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot
import org.eclipse.jdt.internal.core.JavaElement
import org.eclipse.jdt.internal.core.PackageFragment
import org.eclipse.ui.IWorkingSet
import com.typesafe.scalalogging.StrictLogging
import scaps.api.Result
import scaps.api.ValueDef
import scaps.eclipse.core.adapters.ScapsAdapter
import org.eclipse.core.runtime.preferences.InstanceScope
import scaps.eclipse.ScapsPlugin

object ScapsService {
  def apply(indexDir: String): ScapsService = {
    val scapsAdapter = new ScapsAdapter(indexDir)
    new ScapsService(scapsAdapter)
  }
}

class ScapsService(private val scapsAdapter: ScapsAdapter, private val indexDir: String) extends StrictLogging {

  def index(scapsWorkingSet: IWorkingSet): Unit = {

    val (classPath, projectSourceFragmentRoots, librarySourceRootFiles): (List[String], List[IPackageFragmentRoot], List[File]) = {
      def extractClassPath(javaProject: IJavaProject): List[String] = {
        val resolvedClassPath = javaProject.getResolvedClasspath(true)
        resolvedClassPath.map(_.getPath.toString).toList
      }

      def returnElements(classPath: Option[List[String]], projectSourceFragmentRoots: Option[List[IPackageFragmentRoot]], librarySourceRootFiles: Option[List[File]]): (List[String], List[IPackageFragmentRoot], List[File]) = {
        (classPath.getOrElse(List[String]()), projectSourceFragmentRoots.getOrElse(List[IPackageFragmentRoot]()), librarySourceRootFiles.getOrElse(List[File]()))
      }

      val collectedElements = scapsWorkingSet.getElements.toList.map(_ match {
        case javaProject: IJavaProject =>
          val classPath = extractClassPath(javaProject)
          val projectSourceFragmentRoots = javaProject.getAllPackageFragmentRoots.filter(_.getKind == IPackageFragmentRoot.K_SOURCE).toList
          val librarySourceRootFiles = javaProject.getResolvedClasspath(true).filter(_.getSourceAttachmentPath != null).map(_.getSourceAttachmentPath.toFile).toList
          returnElements(Some(classPath), Some(projectSourceFragmentRoots), Some(librarySourceRootFiles))

        case libraryFragementRoot: JarPackageFragmentRoot =>
          if (libraryFragementRoot.getSourceAttachmentPath == null) {
            returnElements(None, None, None)
          } else {
            val classPath = extractClassPath(libraryFragementRoot.getJavaProject)
            returnElements(Some(classPath), None, Some(List(libraryFragementRoot.getSourceAttachmentPath.toFile)))
          }

        case projectSourceFragementRoot: IPackageFragmentRoot =>
          val classPath = extractClassPath(projectSourceFragementRoot.getJavaProject)
          returnElements(None, Some(List(projectSourceFragementRoot)), None)

        case unknownTyp =>
          logger.info("type not supported: " + unknownTyp.getClass)
          returnElements(None, None, None)
      }).unzip3
      (collectedElements._1.flatten.distinct, collectedElements._2.flatten.distinct, collectedElements._3.flatten.distinct)
    }

    def indexProjectTask(monitor: IProgressMonitor): Unit = {
      monitor.setTaskName("Index Project Sources")

      def findSourceFiles(fragmentRoot: IPackageFragmentRoot): Seq[ICompilationUnit] = {
        def recursiveFindSourceFiles(javaElements: Array[IJavaElement]): Array[ICompilationUnit] = {
          val packageFragments = javaElements.collect { case p: PackageFragment => p }
          packageFragments.toList match {
            case Nil => Array()
            case _ =>
              val elements = packageFragments.map { p => (p.getCompilationUnits, p.getChildren) }.unzip
              val sourceFiles = elements._1.flatten
              val subPackageFragments = elements._2.flatten
              sourceFiles ++ recursiveFindSourceFiles(subPackageFragments)
          }
        }
        recursiveFindSourceFiles(fragmentRoot.getChildren)
      }

      val projectSourceFilePaths = projectSourceFragmentRoots.flatMap(findSourceFiles)
      scapsAdapter.indexProject(classPath, projectSourceFilePaths)
    }

    def indexLibrariesTask(monitor: IProgressMonitor): Unit = {
      def indexLibraryTask(monitor: IProgressMonitor, librarySourceRootFile: File): Unit = {
        monitor.setTaskName(librarySourceRootFile.getName)
        scapsAdapter.indexLibrary(classPath, librarySourceRootFile)
      }

      monitor.setTaskName("Index Libraries")
      val subMonitor = SubMonitor.convert(monitor, librarySourceRootFiles.length)
      librarySourceRootFiles.foreach(indexLibraryTask(subMonitor.newChild(1), _))
    }

    def indexFinalize(monitor: IProgressMonitor): Unit = {
      monitor.setTaskName("Finalize Index")
      scapsAdapter.indexFinalize
    }

    new Job("Scaps Indexing") {
      def run(monitor: IProgressMonitor): IStatus = {
        val subMonitor = SubMonitor.convert(monitor, 3)
        indexLibrariesTask(subMonitor.newChild(1))
        indexProjectTask(subMonitor.newChild(1))
        indexFinalize(subMonitor.newChild(1))
        Status.OK_STATUS
      }
    }.schedule
  }

  def search(searchQuery: String): Seq[Result[ValueDef]] = {
    scapsAdapter.search(searchQuery)
  }

}
