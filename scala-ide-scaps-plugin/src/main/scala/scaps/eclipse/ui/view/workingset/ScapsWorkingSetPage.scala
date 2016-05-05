package scaps.eclipse.ui.view.workingset

import org.eclipse.jdt.internal.ui.workingsets.JavaWorkingSetPage
import org.eclipse.jface.viewers.TreeViewer
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Text
import org.eclipse.ui.PlatformUI
import scaps.eclipse.ui.handlers.IndexUCHandler
import org.eclipse.swt.SWT
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.layout.GridData

class ScapsWorkingSetPage extends JavaWorkingSetPage {

  var performBuildCheckbox: Button = _

  override def createControl(composite: Composite): Unit = {
    super.createControl(composite)

    // get the working set name text control and disable it
    for {
      subComposite <- composite.getChildren.collect { case c: Composite => c }.headOption
      workingSetNameText <- subComposite.getChildren.collect { case t: Text => t }.headOption
    } {
      workingSetNameText.setEditable(false)
      workingSetNameText.setEnabled(false)
    }

    for {
      subComposite <- composite.getChildren.collect { case c: Composite => c }.headOption
    } {
      performBuildCheckbox = createPerformBuildCheckbox(subComposite)
    }
  }

  private[workingset] def createPerformBuildCheckbox(parent: Composite) = {
    val performBuildCheckbox = new Button(parent, SWT.CHECK)
    performBuildCheckbox.setText("Build Scaps Index")
    performBuildCheckbox.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL))
    performBuildCheckbox.setSelection(true)
    performBuildCheckbox
  }

  override def configureTree(tree: TreeViewer): Unit = {
    super.configureTree(tree)
    tree.setContentProvider(new ScapsWorkingSetPageContentProvider())
  }

  override def finish(): Unit = {
    super.finish
    if (performBuildCheckbox.getSelection) {
      IndexUCHandler().runIndexer(PlatformUI.getWorkbench.getActiveWorkbenchWindow)
    }
  }

}
