package scaps.eclipse.ui.view.handlers

import org.eclipse.core.commands.AbstractHandler
import org.eclipse.core.commands.ExecutionEvent
import org.eclipse.ui.handlers.HandlerUtil
import scaps.eclipse.ui.view.dialogs.ProjectSelectionDialog
import org.eclipse.ui.internal.ide.actions.BuildUtilities
import org.eclipse.core.resources.IProject
import scaps.eclipse.ui.handlers.IndexUCHandler
import org.eclipse.jdt.core.JavaCore

class IndexHandler extends AbstractHandler {

  def execute(event: ExecutionEvent): Object = {
    val window = HandlerUtil.getActiveWorkbenchWindowChecked(event)
    val selected: Array[IProject] = BuildUtilities.findSelectedProjects(window)
    //    new ProjectSelectionDialog(window, selected).open()
    val projects = selected.filter(_.hasNature(JavaCore.NATURE_ID))
    val javaProjects = projects.map(JavaCore.create)
    IndexUCHandler()(javaProjects)
    null
  }

}
