/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package scaps.eclipse;

import org.eclipse.jface.resource.ImageDescriptor
import org.eclipse.ui.plugin.AbstractUIPlugin
import org.osgi.framework.BundleContext;
import scaps.eclipse.core.services.ScapsService

object ScapsPlugin {

  val PLUGIN_ID = "scala-ide-scaps-plugin"
  val INDEX_RELATIVE_ROOT_DIR = "/.metadata/scaps/"
  val WORKING_SET_NAME = "ScapsWorkingSet"
  val WORKING_SET_PAGE = "scaps.eclipse.ui.view.workingset.ScapsWorkingSetPage"
  val SEARCH_PAGE = "scaps.eclipse.ui.view.search.ScapsSearchPage"

}

/**
 * The activator class controls the plug-in life cycle
 */
class ScapsPlugin extends AbstractUIPlugin {

  override def start(context: BundleContext) {
    super.start(context);
    ScapsService.setIndexerRunning(false)
  }

}
