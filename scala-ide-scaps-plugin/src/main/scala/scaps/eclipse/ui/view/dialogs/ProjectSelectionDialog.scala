package scaps.eclipse.ui.view.dialogs

import org.eclipse.core.resources.IProject
import org.eclipse.jface.dialogs.IDialogConstants
import org.eclipse.jface.dialogs.MessageDialog
import org.eclipse.jface.dialogs.MessageDialog
import org.eclipse.jface.viewers.CheckboxTableViewer
import org.eclipse.jface.viewers.ViewerFilter
import org.eclipse.swt.SWT
import org.eclipse.swt.events.SelectionAdapter
import org.eclipse.swt.events.SelectionListener
import org.eclipse.swt.graphics.Point
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Control
import org.eclipse.ui.IWorkbenchWindow
import org.eclipse.ui.internal.ide.dialogs.ResourceComparator
import org.eclipse.ui.model.WorkbenchContentProvider
import org.eclipse.ui.model.WorkbenchLabelProvider
import org.eclipse.jface.viewers.Viewer
import org.eclipse.core.resources.IncrementalProjectBuilder
import org.eclipse.ui.internal.ide.actions.BuildUtilities
import org.eclipse.jface.viewers.ICheckStateListener
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jface.viewers.CheckStateChangedEvent
import org.eclipse.swt.events.SelectionEvent

class ProjectSelectionDialog(var window: IWorkbenchWindow, var selected: Array[IProject]) extends MessageDialog(window.getShell, "Project Indexer", null, "The project indexer allows you to use the Scaps Search", MessageDialog.NONE, Array("Index now", "Cancel"), 0) {

  private var projectNames: CheckboxTableViewer = _

  private var allButton, selectedButton, indexNowButton, globalIndexButton, projectIndexButton: Button = _

  override def buttonPressed(buttonId: Int): Unit = {
    val cleanAll = allButton.getSelection()
    val buildAll = indexNowButton != null && indexNowButton.getSelection()
    val globalBuild = globalIndexButton != null && globalIndexButton.getSelection()
    super.buttonPressed(buttonId)
    if (buttonId != IDialogConstants.OK_ID) {
      return
    }
    //save all dirty editors
    BuildUtilities.saveEditors(null);
    //batching changes ensures that autobuild runs after cleaning
  }

  override def createCustomArea(parent: Composite): Control = {
    val area = new Composite(parent, SWT.NONE)
    area.setFont(parent.getFont)
    val layout = new GridLayout()
    layout.marginWidth = 0
    layout.marginHeight = 0
    layout.makeColumnsEqualWidth = true
    area.setLayout(layout)
    area.setLayoutData(new GridData(GridData.FILL_BOTH))
    val updateEnablement: SelectionListener = new SelectionAdapter() {
      def widgetSelected(): Unit = {
        updateEnablemente()
      }
    }

    //    val settings = getDialogBoundsSettings
    //    val selectSelectedButton = settings.getBoolean("TOGGLE_SELECTED")

    allButton = new Button(area, SWT.RADIO)
    allButton.setText("Index all projects");
    //    allButton.setSelection(!selectSelectedButton);
    allButton.addSelectionListener(updateEnablement);
    selectedButton = new Button(area, SWT.RADIO);
    selectedButton.setText("Index project selected below");
    //    selectedButton.setSelection(selectSelectedButton);
    selectedButton.addSelectionListener(updateEnablement);

    createProjectSelectionTable(area)

    if (!ResourcesPlugin.getWorkspace().isAutoBuilding()) {
      indexNowButton = new Button(parent, SWT.CHECK)
      indexNowButton.setText("MAL LUEGE")
      //      val buildNow = settings.get("BUILD_NOW")
      //      indexNowButton.setSelection(buildNow == null || buildNow.toBoolean)
      indexNowButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING))
      indexNowButton.addSelectionListener(updateEnablement)

      globalIndexButton = new Button(parent, SWT.RADIO)
      globalIndexButton.setText("Global Index")
      //      val buildAll = settings.get("BUILD_ALL")
      //      globalIndexButton.setSelection(buildAll == null || buildAll.toBoolean);
      var data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING)
      data.horizontalIndent = 10
      globalIndexButton.setLayoutData(data)
      globalIndexButton.setEnabled(indexNowButton.getSelection())

      projectIndexButton = new Button(parent, SWT.RADIO)
      projectIndexButton.setText("Build selected projects button")
      projectIndexButton.setSelection(!globalIndexButton.getSelection())
      data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING)
      data.horizontalIndent = 10
      projectIndexButton.setLayoutData(data)
      projectIndexButton.setEnabled(indexNowButton.getSelection())

      val buildRadioSelected: SelectionListener = new SelectionAdapter() {
        override def widgetSelected(e: SelectionEvent): Unit = {
          updateBuildRadioEnablement()
        }
      }
      globalIndexButton.addSelectionListener(buildRadioSelected)
      projectIndexButton.addSelectionListener(buildRadioSelected)
    }

    area
  }

  def updateBuildRadioEnablement(): Unit = {
    projectIndexButton.setSelection(!projectIndexButton.getSelection());
  }

  def createProjectSelectionTable(radioGroup: Composite): Unit = {
    projectNames = CheckboxTableViewer.newCheckList(radioGroup, SWT.BORDER)
    projectNames.setContentProvider(new WorkbenchContentProvider())
    projectNames.setLabelProvider(new WorkbenchLabelProvider())
    projectNames.setComparator(new ResourceComparator(ResourceComparator.NAME))
    projectNames.addFilter(new ViewerFilter() {
      val projectHolder = new Array[IProject](1)
      override def select(viewer: Viewer, parentElement: Object, element: Object): Boolean = {
        val project: IProject =
          element match {
            case iProject: IProject => iProject
            case _                  => null
          }

        if (project != null && project.isAccessible()) {
          projectHolder(0) = project
          BuildUtilities.isEnabled(projectHolder, IncrementalProjectBuilder.CLEAN_BUILD);
        } else {
          false
        }
      }
    })
    projectNames.setInput(ResourcesPlugin.getWorkspace().getRoot())
    val data = new GridData(GridData.FILL_BOTH)
    data.horizontalSpan = 2
    data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH
    data.heightHint = IDialogConstants.ENTRY_FIELD_WIDTH
    projectNames.getTable().setLayoutData(data)
    projectNames.setCheckedElements(selected.asInstanceOf[Array[Object]])
    val checked = projectNames.getCheckedElements()
    // reveal first checked project unless in "all projects" mode
    if (checked.length > 0 && !allButton.getSelection()) {
      projectNames.reveal(checked(0));
    }
    //table is disabled to start because all button is selected
    projectNames.getTable().setEnabled(selectedButton.getSelection())
    projectNames.addCheckStateListener(new ICheckStateListener() {
      override def checkStateChanged(event: CheckStateChangedEvent) {
        selected = projectNames.getCheckedElements().asInstanceOf[Array[IProject]]
        updateEnablemente()
      }
    })
  }

  override def createContents(parent: Composite): Control = {
    val content = super.createContents(parent)
    updateEnablemente()
    content
  }

  override def close(): Boolean = {
    super.close()
  }

  override def getInitialLocation(initialSize: Point): Point = {
    new Point(50, 50)
  }

  def updateEnablemente(): Unit = {
    projectNames.getTable().setEnabled(selectedButton.getSelection())
    val enabled = allButton.getSelection() || selected.length > 0
    getButton(IDialogConstants.OK_ID).setEnabled(enabled)
    if (globalIndexButton != null) {
      globalIndexButton.setEnabled(indexNowButton.getSelection())
    }
    if (projectIndexButton != null) {
      projectIndexButton.setEnabled(indexNowButton.getSelection())
    }
  }

}
