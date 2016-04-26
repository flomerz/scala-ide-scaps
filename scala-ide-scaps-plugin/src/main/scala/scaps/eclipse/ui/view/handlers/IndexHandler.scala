package scaps.eclipse.ui.view.handlers

import org.eclipse.core.commands.AbstractHandler
import org.eclipse.core.commands.ExecutionEvent
import org.eclipse.ui.PlatformUI
import scaps.eclipse.ui.handlers.IndexUCHandler

class IndexHandler extends AbstractHandler {

  def execute(event: ExecutionEvent): Object = {
    val SCAPS_WORKING_SET_NAME = "ScapsWorkingSet"
    val scapsWorkingSet = Option(PlatformUI.getWorkbench.getWorkingSetManager.getWorkingSet(SCAPS_WORKING_SET_NAME)).getOrElse {
      val newScapsWorkingSet = IndexUCHandler().showProjectSelectionDialog(event)
      newScapsWorkingSet
    }
    // TODO: falls doch keine Selektionen gemacht wurden, darf die Indexierung nicht stattfinden!
    IndexUCHandler().selectIProjects(scapsWorkingSet)
    null
  }

}
