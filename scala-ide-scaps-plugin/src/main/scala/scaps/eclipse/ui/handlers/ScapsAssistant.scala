package scaps.eclipse.ui.handlers

import org.eclipse.core.commands.AbstractHandler
import org.eclipse.core.commands.ExecutionEvent
import org.eclipse.ui.IWorkbenchWindow
import org.eclipse.jface.dialogs.MessageDialog
import org.eclipse.ui.handlers.HandlerUtil

class ScapsAssistant extends AbstractHandler {

  def execute(event: ExecutionEvent): Object = {
    val window = HandlerUtil.getActiveWorkbenchWindowChecked(event)
		MessageDialog.openInformation(
				window.getShell(),
				"ScalaSearchIDE",
				"Scaps Assistant")
		return null;
  }
}