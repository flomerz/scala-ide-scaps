package scaps.eclipse.ui.handlers

import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jdt.core.IJavaProject
import scaps.eclipse.core.services.ScapsService
import org.eclipse.jdt.core.IPackageFragmentRoot
import org.eclipse.core.commands.ExecutionEvent
import org.eclipse.ui.internal.WorkingSet
import org.eclipse.jface.wizard.WizardDialog
import org.eclipse.ui.PlatformUI
import org.eclipse.jdt.internal.ui.workingsets.IWorkingSetIDs
import org.eclipse.ui.handlers.HandlerUtil
import org.eclipse.ui.IWorkingSet
import org.eclipse.core.resources.IProject

object IndexUCHandler extends AbstractUCHandler {
  def apply(): IndexUCHandler = {
    new IndexUCHandler(ScapsService(_indexDir))
  }
}

class IndexUCHandler(private val scapsService: ScapsService) {

  def apply(projects: Seq[IJavaProject]): Unit = {
    projects.map { project =>
      val resolvedClassPath = project.getResolvedClasspath(true)

      val classPath = resolvedClassPath.map(_.getPath.toString).toList
      val librarySourceRootFiles = resolvedClassPath.filter(_.getSourceAttachmentPath != null).map(_.getSourceAttachmentPath.toFile)

      val projectSourceFragmentRoots = project.getAllPackageFragmentRoots.filter(_.getKind == IPackageFragmentRoot.K_SOURCE)
      scapsService.index(classPath, projectSourceFragmentRoots, librarySourceRootFiles)
    }
  }

  def showProjectSelectionDialog(event: ExecutionEvent): IWorkingSet = {
    val window = HandlerUtil.getActiveWorkbenchWindowChecked(event)
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
    scapsWorkingSet
  }

  def selectIProjects(workingSet: IWorkingSet): Unit = {
    val elements = workingSet.getElements
    val iProjects: Array[IJavaProject] = {
      elements.collect(_ match { case a: IJavaProject => a })
    }
    apply(iProjects.toSeq)
  }

}
