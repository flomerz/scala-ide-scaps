package scaps.eclipse.ui.handlers

import org.eclipse.jdt.core.IJavaProject

import scaps.eclipse.core.adapters.ScapsAdapter
import scaps.eclipse.core.services.ScapsService

object IndexUCHandler {
  def apply: IndexUCHandler = {
    val indexDir = ""
    new IndexUCHandler(ScapsService(indexDir))
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
