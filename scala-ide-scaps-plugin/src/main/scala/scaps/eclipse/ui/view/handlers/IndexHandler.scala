package scaps.eclipse.ui.view.handlers

import org.eclipse.core.commands.AbstractHandler
import org.eclipse.core.commands.ExecutionEvent
import org.eclipse.jdt.internal.ui.workingsets.IWorkingSetIDs
import org.eclipse.jface.wizard.WizardDialog
import org.eclipse.ui.handlers.HandlerUtil
import org.eclipse.ui.internal.dialogs.WorkingSetNewWizard
import org.eclipse.ui.internal.AbstractWorkingSetManager
import org.eclipse.ui.PlatformUI
import org.eclipse.ui.internal.WorkingSet

class IndexHandler extends AbstractHandler {

  def execute(event: ExecutionEvent): Object = {
    val window = HandlerUtil.getActiveWorkbenchWindowChecked(event)
    //    val selected: Array[IProject] = BuildUtilities.findSelectedProjects(window)
    //    new ProjectSelectionDialog(window, selected).open()
    //    val projects = selected.filter(_.hasNature(JavaCore.NATURE_ID))
    //    val javaProjects = projects.map(JavaCore.create)
    //    IndexUCHandler()(javaProjects)

    //    new JavaWorkingSetPage().createControl()

    //    val registry = WorkbenchPlugin.getDefault().getWorkingSetRegistry()
    //    val jwsp = new JavaWorkingSetPage()
    //    val workingSetPage = registry.getWorkingSetPage("org.eclipse.jdt.ui.JavaWorkingSetPage")
    //    val o = PreferencesUtil.createPreferenceDialogOn(window.getShell, "org.eclipse.jdt.internal.ui.workingsets.JavaWorkingSetPage", null, null)
    //    o.open()
    //    val x = PlatformUI.getWorkbench().getWorkingSetManager().createWorkingSet("Project Selection Indexer", null)
    //    PlatformUI.getWorkbench.getWorkingSetManager.createWorkingSetSelectionDialog(window.getShell, true).open()
    //createWorkingSetEditWizard(fWorkingSet)
    val workingSetManager = PlatformUI.getWorkbench.getWorkingSetManager
    val SCAPS_WORKING_SET_NAME = "ScapsWorkingSet"

    val scapsWorkingSet = Option(workingSetManager.getWorkingSet(SCAPS_WORKING_SET_NAME)).getOrElse {
      val newScapsWorkingSet = workingSetManager.createWorkingSet(SCAPS_WORKING_SET_NAME, Array())
      newScapsWorkingSet.setId(IWorkingSetIDs.JAVA)
      workingSetManager.addWorkingSet(newScapsWorkingSet)
      newScapsWorkingSet
    }
    val workingSetWizard = workingSetManager.createWorkingSetEditWizard(scapsWorkingSet)
    val dialog = new WizardDialog(window.getShell, workingSetWizard)
    dialog.create()
    dialog.open()
    null
  }

}
