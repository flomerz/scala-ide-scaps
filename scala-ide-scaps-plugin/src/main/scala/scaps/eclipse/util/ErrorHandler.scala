package scaps.eclipse.util

import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Status
import org.eclipse.jface.dialogs.MessageDialog
import org.eclipse.ui.progress.UIJob

import com.typesafe.scalalogging.StrictLogging

import scalaz.{ -\/ => -\/ }
import scalaz.{ \/ => \/ }
import scaps.eclipse.core.adapters.ScapsEngineError
import scaps.eclipse.core.adapters.ScapsError
import scaps.eclipse.core.adapters.ScapsSearchError
import scaps.eclipse.core.adapters.ScapsSearchQueryError
import scaps.searchEngine.QueryError

object ErrorHandler extends StrictLogging {

  def apply(scapsError: ScapsError): Unit = {
    def log(msg: String, throwable: Throwable): String = {
      logger.error(msg, throwable)
      msg
    }
    val msg = scapsError match {
      case ScapsEngineError(t) => log("Scaps Search Engine couldn't be loaded!", t)
      case ScapsSearchError(t) => log("Error while searching Scaps!", t)
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
        MessageDialog.openError(Util.getWorkbenchWindow.getShell, "Scaps Plugin", msg)
        Status.OK_STATUS
      }
    }.schedule

  }

}
