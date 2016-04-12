package scaps.eclipse.ui.handlers

import org.eclipse.core.resources.ResourcesPlugin

abstract class AbstractUCHandler {
  lazy val _indexDir = ResourcesPlugin.getWorkspace.getRoot.getLocation.toString + ".metadata/scaps"
}
