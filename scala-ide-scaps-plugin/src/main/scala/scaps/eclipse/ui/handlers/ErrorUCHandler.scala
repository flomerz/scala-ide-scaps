package scaps.eclipse.ui.handlers

import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Status
import org.eclipse.jface.dialogs.MessageDialog
import org.eclipse.ui.progress.UIJob

import scaps.eclipse.core.adapters.ScapsEngineError
import scaps.eclipse.core.adapters.ScapsError
import scaps.eclipse.core.adapters.ScapsSearchError
import scaps.eclipse.core.adapters.ScapsSearchQueryError
import scaps.searchEngine.QueryError
import scaps.eclipse.util.Util
import scaps.eclipse.core.services.ScapsService
import com.typesafe.scalalogging.StrictLogging

object ErrorUCHandler extends StrictLogging {

  def apply(scapsError: ScapsError): Unit = {
    def log(msg: String, throwable: Throwable): String = {
      logger.error(msg, throwable)
      msg
    }
    val msg = scapsError match {
      case ScapsEngineError(t) => log("Scaps Search Engine couldn't be loaded!", t)
      case ScapsSearchError(t) => log("Error while searching Scaps!", t)
      case ScapsSearchQueryError(queryError: QueryError) => {
        logger.error(queryError.toString)
        "Error with Query"
      }
      case _ => {
        logger.error(scapsError.toString)
        "Something went wrong!"
      }
    }

    new UIJob("Show Scaps Plugin Error") {
      def runInUIThread(monitor: IProgressMonitor): IStatus = {
        MessageDialog.openError(Util.getWorkbenchWindow.getShell, "Scaps Plugin", msg)
        Status.OK_STATUS
      }
    }.schedule

    ScapsService.setIndexerRunning(false)
  }

}
