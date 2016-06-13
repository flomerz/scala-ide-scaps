/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package scaps.eclipse.core.util

import org.eclipse.core.runtime.ILog
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Platform
import org.eclipse.core.runtime.Status
import org.eclipse.jface.dialogs.MessageDialog
import org.eclipse.ui.PlatformUI
import org.eclipse.ui.progress.UIJob

import com.typesafe.scalalogging.StrictLogging

import scaps.eclipse.ScapsPlugin
import scaps.eclipse.core.adapters.ScapsEngineError
import scaps.eclipse.core.adapters.ScapsError
import scaps.eclipse.core.adapters.ScapsIndexError
import scaps.eclipse.core.adapters.ScapsSearchError
import scaps.eclipse.core.adapters.ScapsSearchQueryError
import scaps.searchEngine.QueryError

object ErrorHandler extends StrictLogging {

  def apply(scapsError: ScapsError): Unit = {
    def log(msg: String, throwable: Throwable): String = {
      logger.error(msg, throwable)
      logToErrorLog(msg, throwable)
      msg
    }
    val msg = scapsError match {
      case ScapsEngineError(t) => log("Scaps Search Engine couldn't be loaded!", t)
      case ScapsSearchError(t) => log("Error while searching Scaps!", t)
      case ScapsIndexError(t)  => log("Error while indexing Scaps!", t)
      case ScapsSearchQueryError(queryError: QueryError) =>
        val queryErrorStr = queryError.toString
        logger.error(queryErrorStr)
        s"Error with the Query: $queryErrorStr"
      case _ =>
        val scapsErrorStr = scapsError.toString
        logger.error(scapsErrorStr)
        s"Something went wrong: $scapsErrorStr"
    }

    new UIJob("Show Scaps Plugin Error") {
      def runInUIThread(monitor: IProgressMonitor): IStatus = {
        val window = PlatformUI.getWorkbench.getWorkbenchWindows.head
        MessageDialog.openError(window.getShell, "Scaps Plugin", msg)
        Status.OK_STATUS
      }
    }.schedule

  }

  private def getLog: ILog = {
    val bundle = Platform.getBundle(ScapsPlugin.PLUGIN_ID)
    Platform.getLog(bundle)
  }

  def logToErrorLog(msg: String, throwable: Throwable): Unit =
    getLog.log(new Status(IStatus.ERROR, ScapsPlugin.PLUGIN_ID, msg, throwable))

}
