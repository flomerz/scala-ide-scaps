package scaps.eclipse.ui.view.handlers

import org.eclipse.core.commands.AbstractHandler
import org.eclipse.core.commands.ExecutionEvent
import scaps.eclipse.ui.handlers.IndexUCHandler
import org.eclipse.ui.handlers.HandlerUtil

class ScapsConfigureIndexerHandler extends AbstractHandler {

  def execute(event: ExecutionEvent): Object = {
    val window = HandlerUtil.getActiveWorkbenchWindowChecked(event)
    IndexUCHandler().configureIndexer(window)
    null
  }

}
