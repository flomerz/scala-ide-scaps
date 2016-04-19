package scaps.eclipse.ui.handlers

import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jdt.core.IJavaProject
import scaps.eclipse.core.services.ScapsService
import org.eclipse.jdt.core.IPackageFragmentRoot

object IndexUCHandler extends AbstractUCHandler {
  def apply(): IndexUCHandler = {
    new IndexUCHandler(ScapsService(_indexDir))
  }
}

class IndexUCHandler(private val scapsService: ScapsService) {

  def apply(projects: Seq[IJavaProject]): Unit = {
    projects.map { project =>
      val resolvedClassPath = project.getResolvedClasspath(true)

      val classPath = resolvedClassPath.map(_.getPath.toString).toList
      val librarySourceRootFiles = resolvedClassPath.filter(_.getSourceAttachmentPath != null).map(_.getSourceAttachmentPath.toFile)

      val projectSourceFragmentRoots = project.getAllPackageFragmentRoots.filter(_.getKind == IPackageFragmentRoot.K_SOURCE)
      scapsService.index(classPath, projectSourceFragmentRoots, librarySourceRootFiles)
    }
  }

}
