package scaps.eclipse.core.services

import org.eclipse.core.runtime.IStatus
import org.eclipse.jdt.internal.core.PackageFragment
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.core.IPackageFragmentRoot
import org.eclipse.ui.IWorkingSet
import org.eclipse.core.runtime.jobs.Job
import org.eclipse.core.runtime.SubMonitor
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.ICompilationUnit
import java.io.File
import org.eclipse.core.runtime.Status
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot
import com.typesafe.scalalogging.StrictLogging
import scaps.eclipse.core.adapters.ScapsAdapter
import scaps.eclipse.util.Util

class ScapsIndexService(private val scapsAdapter: ScapsAdapter) extends StrictLogging {

  def apply(scapsWorkingSet: IWorkingSet): Unit = {
    ScapsService.setIndexerRunning(true)

    new Job("Scaps Indexer") {
      def run(monitor: IProgressMonitor): IStatus = {
        val subMonitor = SubMonitor.convert(monitor, 3)
        monitor.setTaskName("Collect Elements")
        val (classPath, projectSourceFragmentRoots, librarySourceRootFiles) = extractElements(scapsWorkingSet)
        scapsAdapter.indexReset
        Util.logTime(logger.info(_), "indexing libraries", indexLibrariesTask(subMonitor.newChild(1), classPath, librarySourceRootFiles))
        Util.logTime(logger.info(_), "indexing project sources", indexProjectTask(subMonitor.newChild(1), classPath, projectSourceFragmentRoots))
        Util.logTime(logger.info(_), "index finalize", indexFinalize(subMonitor.newChild(1)))
        ScapsService.swapIndexDirs
        ScapsService.setIndexerRunning(false)
        Status.OK_STATUS
      }
    }.schedule
  }

  private[services] def extractElements(scapsWorkingSet: IWorkingSet): (List[String], List[IPackageFragmentRoot], List[File]) = {
    def extractClassPath(javaProject: IJavaProject): List[String] = {
      val resolvedClassPath = javaProject.getResolvedClasspath(true)
      resolvedClassPath.map(_.getPath.toString).toList
    }

    def returnElements(
      classPath: Option[List[String]],
      projectSourceFragmentRoots: Option[List[IPackageFragmentRoot]],
      librarySourceRootFiles: Option[List[File]]): (List[String], List[IPackageFragmentRoot], List[File]) = {
      (classPath.getOrElse(List.empty), projectSourceFragmentRoots.getOrElse(List.empty), librarySourceRootFiles.getOrElse(List.empty))
    }

    val (classPaths, projectSourceFragmentRoots, librarySourceRootFiles) = scapsWorkingSet.getElements.toList.map {
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
        returnElements(Some(classPath), Some(List(projectSourceFragementRoot)), None)

      case unknownTyp =>
        logger.info("type not supported: " + unknownTyp.getClass)
        returnElements(None, None, None)
    }.unzip3

    (classPaths.flatten.distinct, projectSourceFragmentRoots.flatten.distinct, librarySourceRootFiles.flatten.distinct)
  }

  private def indexProjectTask(monitor: IProgressMonitor, classPath: List[String], projectSourceFragmentRoots: List[IPackageFragmentRoot]): Unit = {
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

  private def indexLibrariesTask(monitor: IProgressMonitor, classPath: List[String], librarySourceRootFiles: List[File]): Unit = {
    def indexLibraryTask(monitor: IProgressMonitor, librarySourceRootFile: File): Unit = {
      monitor.setTaskName("Library: " + librarySourceRootFile.getPath)
      scapsAdapter.indexLibrary(classPath, librarySourceRootFile)
    }

    monitor.setTaskName("Index Libraries")
    val subMonitor = SubMonitor.convert(monitor, librarySourceRootFiles.length)
    librarySourceRootFiles.foreach(indexLibraryTask(subMonitor.newChild(1), _))
  }

  private def indexFinalize(monitor: IProgressMonitor): Unit = {
    monitor.setTaskName("Finalize Index")
    scapsAdapter.indexFinalize
  }

}
