/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package scaps.eclipse.ui.view.actions

import org.eclipse.jface.action.IAction
import org.eclipse.jface.viewers.ISelection
import org.eclipse.ui.IWorkbenchWindow
import org.eclipse.ui.IWorkbenchWindowActionDelegate

import scaps.eclipse.ui.handlers.SearchUCHandler

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
    SearchUCHandler().openSearchDialog(window)

  }

  def selectionChanged(action: IAction, selection: ISelection): Unit = {}

  def dispose(): Unit = {
    window = null
  }

}
