package scaps.eclipse.ui.view.actions

import org.eclipse.ui.IWorkbenchWindowActionDelegate
import org.eclipse.jface.action.IAction
import org.eclipse.jface.viewers.ISelection
import org.eclipse.ui.IWorkbenchWindow
import org.eclipse.search.internal.ui.SearchPlugin
import org.eclipse.search.ui.NewSearchUI

class ScalaSearchAction extends IWorkbenchWindowActionDelegate {

  private var window: IWorkbenchWindow = _
  private var pageID = "scaps.eclipse.ui.pages.ScalaSearchPage"

  def init(window: IWorkbenchWindow) {
    this.window = window
  }

  def run(action: IAction) {
    getWorkbenchWindow
    if (window.getActivePage == null) {
      print("Run: Something is not good!")
      return
    }
    NewSearchUI.openSearchDialog(window, pageID)
  }

  def selectionChanged(action: IAction, selection: ISelection) {
  }

  def dispose {
    window = null
  }

  def getWorkbenchWindow {
    if (window == null) {
      window = SearchPlugin.getActiveWorkbenchWindow
    }
  }

}