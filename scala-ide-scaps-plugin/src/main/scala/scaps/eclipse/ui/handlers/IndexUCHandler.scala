package scaps.eclipse.ui.handlers

import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jdt.core.IJavaProject

import scaps.eclipse.core.services.ScapsService

object IndexUCHandler extends AbstractUCHandler {
  def apply(): IndexUCHandler = {
    new IndexUCHandler(ScapsService(_indexDir))
  }
}

class IndexUCHandler(private val scapsService: ScapsService) {

  def apply(project: IJavaProject) {
    val resolvedClassPath = project.getResolvedClasspath(true)
    val classPath = resolvedClassPath.map(_.getPath.toString).toList
    val projectSourcePaths = resolvedClassPath.filter(_.getSourceAttachmentPath != null).map(_.getSourceAttachmentPath.toString)
    scapsService.index(classPath, projectSourcePaths, classPath)
  }

}
