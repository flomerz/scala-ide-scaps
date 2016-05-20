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

object IndexUCHandler {
  // Bringt diese Methode etwas?
  private def INSTANCE = new IndexUCHandler(ScapsService.INDEXING)
  def apply(): IndexUCHandler = INSTANCE
}

class IndexUCHandler(private val scapsIndexService: ScapsIndexService) extends StrictLogging {

  lazy val workingSetManager = PlatformUI.getWorkbench.getWorkingSetManager

  def runIndexer(window: IWorkbenchWindow): Unit = {
    if (ScapsService.isIndexerRunning) {
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
