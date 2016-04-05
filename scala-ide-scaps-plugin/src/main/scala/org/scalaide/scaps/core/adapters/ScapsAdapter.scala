package org.scalaide.scaps.core.adapters

import org.eclipse.core.resources.ResourcesPlugin

import scaps.searchEngine.SearchEngine
import scaps.settings.Settings

class ScapsAdapter {
  private val searchEngine = {
    val workspacePath = ResourcesPlugin.getWorkspace.getRoot.getLocation
    val indexDir = workspacePath + "/.metadata/scaps"
    var conf = Settings.fromApplicationConf.modIndex { index => index.copy(indexDir = indexDir) }
    SearchEngine(conf).get
  }
}