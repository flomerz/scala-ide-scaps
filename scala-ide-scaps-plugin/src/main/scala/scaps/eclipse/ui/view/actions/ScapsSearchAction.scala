package scaps.eclipse.ui.view.actions

import org.eclipse.jface.action.IAction
import org.eclipse.jface.viewers.ISelection
import org.eclipse.search.internal.ui.SearchPlugin
import org.eclipse.search.ui.NewSearchUI
import org.eclipse.ui.IWorkbenchWindow
import org.eclipse.ui.IWorkbenchWindowActionDelegate
import scaps.eclipse.ScapsPlugin

class ScapsSearchAction extends IWorkbenchWindowActionDelegate {

  private var window: IWorkbenchWindow = _

  def init(window: IWorkbenchWindow): Unit = {
    this.window = window
  }

  def run(action: IAction): Unit = {
    if (window.getActivePage == null) {
      // TODO: ErrorHandling
      print("Run: Something is not good!")
      return
    }

  }

  def selectionChanged(action: IAction, selection: ISelection): Unit = {}

  def dispose(): Unit = {
    window = null
  }

}
