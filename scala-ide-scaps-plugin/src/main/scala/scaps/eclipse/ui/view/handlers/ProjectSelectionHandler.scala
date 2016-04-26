package scaps.eclipse.ui.view.handlers

import org.eclipse.core.commands.AbstractHandler
import org.eclipse.core.commands.ExecutionEvent

import scaps.eclipse.ui.handlers.IndexUCHandler

class ProjectSelectionHandler extends AbstractHandler {

  def execute(event: ExecutionEvent): Object = {
    val workingSet = IndexUCHandler().showProjectSelectionDialog(event)
    //    TODO: Bei cancel Button darf nicht indexiert werden!
    IndexUCHandler().selectIProjects(workingSet)
    null
  }
}
