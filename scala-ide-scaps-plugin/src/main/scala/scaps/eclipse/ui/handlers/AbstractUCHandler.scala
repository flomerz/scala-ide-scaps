package scaps.eclipse.ui.handlers

import org.eclipse.core.resources.ResourcesPlugin

abstract class AbstractUCHandler {

  lazy val SCAPS_INDEX_DIR = {
    ResourcesPlugin.getWorkspace.getRoot.getLocation.toString + "/.metadata/scaps"
  }

  val SCAPS_WORKING_SET_NAME = "ScapsWorkingSet"

}
