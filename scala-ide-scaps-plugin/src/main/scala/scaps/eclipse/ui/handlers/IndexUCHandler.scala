package scaps.eclipse.ui.handlers

import org.eclipse.core.commands.ExecutionEvent
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.internal.ui.workingsets.IWorkingSetIDs
import org.eclipse.jface.wizard.WizardDialog
import org.eclipse.ui.IWorkingSet
import org.eclipse.ui.PlatformUI
import org.eclipse.ui.handlers.HandlerUtil

import com.typesafe.scalalogging.StrictLogging

import scaps.eclipse.core.services.ScapsIndexService
import scaps.eclipse.core.services.ScapsService

object IndexUCHandler extends AbstractUCHandler {
  def apply(): IndexUCHandler = {
    new IndexUCHandler(ScapsService.INDEXING)
  }
}

class IndexUCHandler(private val scapsIndexService: ScapsIndexService) extends AbstractUCHandler with StrictLogging {

  lazy val workingSetManager = PlatformUI.getWorkbench.getWorkingSetManager

  def createIndex(): Unit = {
    val scapsWorkingSet = workingSetManager.getWorkingSet(SCAPS_WORKING_SET_NAME)
    scapsIndexService(scapsWorkingSet)
  }

  def showProjectSelectionDialog(event: ExecutionEvent): IWorkingSet = {
    val window = HandlerUtil.getActiveWorkbenchWindowChecked(event)
    val workingSetManager = PlatformUI.getWorkbench.getWorkingSetManager
    val scapsWorkingSet = Option(workingSetManager.getWorkingSet(SCAPS_WORKING_SET_NAME)).getOrElse {
      val newScapsWorkingSet = workingSetManager.createWorkingSet(SCAPS_WORKING_SET_NAME, Array())
      newScapsWorkingSet.setId("scaps.eclipse.ui.view.workingset.ScapsWorkingSetPage")
      workingSetManager.addWorkingSet(newScapsWorkingSet)
      newScapsWorkingSet
    }
    val workingSetWizard = workingSetManager.createWorkingSetEditWizard(scapsWorkingSet)
    val dialog = new WizardDialog(window.getShell, workingSetWizard)
    dialog.create()
    dialog.open()
    scapsWorkingSet
  }

  def selectIProjects(workingSet: IWorkingSet): Unit = {
    val elements = workingSet.getElements
    val iProjects: Array[IJavaProject] = {
      elements.collect(_ match { case a: IJavaProject => a })
    }
  }

}
