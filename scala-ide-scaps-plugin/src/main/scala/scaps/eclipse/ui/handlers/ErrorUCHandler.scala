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

object ErrorUCHandler {

  def apply(scapsError: ScapsError): Unit = {
    val msg = scapsError match {
      case ScapsEngineError(_) => "Scaps Search Engine couldn't be loaded!"
      case ScapsSearchError(_) => "Error while searching Scaps!"
      case ScapsSearchQueryError(queryError: QueryError) => "Error with Query"
      case _ => "Something went wrong!"
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
