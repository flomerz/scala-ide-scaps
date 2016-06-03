/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package scaps.eclipse;

import org.eclipse.core.runtime.ILog
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Platform
import org.eclipse.core.runtime.Status
import org.eclipse.ui.plugin.AbstractUIPlugin
import org.osgi.framework.BundleContext

import scaps.eclipse.core.services.ScapsSettingsService

object ScapsPlugin {

  val PLUGIN_ID = "scala-ide-scaps-plugin"
  val INDEX_RELATIVE_ROOT_DIR = "/.metadata/scaps/"
  val WORKING_SET_NAME = "ScapsWorkingSet"
  val WORKING_SET_PAGE = "scaps.eclipse.ui.view.workingset.ScapsWorkingSetPage"
  val SEARCH_PAGE = "scaps.eclipse.ui.view.search.ScapsSearchPage"

  def getLog: ILog = {
    val bundle = Platform.getBundle(PLUGIN_ID)
    Platform.getLog(bundle)
  }

  def log(msg: String, throwable: Throwable): Unit =
    getLog.log(new Status(IStatus.ERROR, PLUGIN_ID, msg, throwable))

}

/**
 * The activator class controls the plug-in life cycle
 */
class ScapsPlugin extends AbstractUIPlugin {

  override def start(context: BundleContext) {
    super.start(context);
    ScapsSettingsService.setIndexerRunning(false)
  }

}
