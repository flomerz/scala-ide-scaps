/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package scaps.eclipse.ui.handlers

import org.eclipse.jface.dialogs.MessageDialog
import org.eclipse.jface.wizard.WizardDialog
import org.eclipse.ui.IWorkbenchWindow
import org.eclipse.ui.IWorkingSet
import org.eclipse.ui.PlatformUI

import com.typesafe.scalalogging.StrictLogging

import scaps.eclipse.ScapsPlugin
import scaps.eclipse.core.services.ScapsIndexService
import scaps.eclipse.core.services.ScapsService
import scaps.eclipse.core.services.ScapsSettingsService

object IndexUCHandler {
  def apply(): IndexUCHandler = new IndexUCHandler(ScapsService.createIndexService)
}

class IndexUCHandler(scapsIndexService: ScapsIndexService) extends StrictLogging {

  lazy val workingSetManager = PlatformUI.getWorkbench.getWorkingSetManager

  def runIndexer(window: IWorkbenchWindow): Unit = {
    if (ScapsSettingsService.isIndexerRunning) {
      MessageDialog.openInformation(window.getShell(),
        "Scaps Indexer",
        "The Scaps Indexer is already running, please wait until it's done.");
    } else {
      val scapsWorkingSet = Option(workingSetManager.getWorkingSet(ScapsPlugin.WORKING_SET_NAME))
      if (scapsWorkingSet.isEmpty) {
        configureIndexer(window)
      } else {
        scapsIndexService(scapsWorkingSet.get)
      }
    }
  }

  def configureIndexer(window: IWorkbenchWindow): IWorkingSet = {
    val scapsWorkingSet = Option(workingSetManager.getWorkingSet(ScapsPlugin.WORKING_SET_NAME)).getOrElse {
      val newScapsWorkingSet = workingSetManager.createWorkingSet(ScapsPlugin.WORKING_SET_NAME, Array())
      newScapsWorkingSet.setId(ScapsPlugin.WORKING_SET_PAGE)
      workingSetManager.addWorkingSet(newScapsWorkingSet)
      newScapsWorkingSet
    }
    val workingSetWizard = workingSetManager.createWorkingSetEditWizard(scapsWorkingSet)
    val dialog = new WizardDialog(window.getShell, workingSetWizard)
    dialog.create()
    dialog.open()
    scapsWorkingSet
  }

}
