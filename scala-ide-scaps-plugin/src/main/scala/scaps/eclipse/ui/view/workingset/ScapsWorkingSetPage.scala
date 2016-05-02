package scaps.eclipse.ui.view.workingset

import org.eclipse.jdt.core.IPackageFragmentRoot
import org.eclipse.jdt.internal.ui.workingsets.JavaWorkingSetPage
import org.eclipse.jdt.ui.StandardJavaElementContentProvider
import org.eclipse.jface.viewers.TreeViewer
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Text
import scaps.eclipse.ui.handlers.IndexUCHandler
import org.eclipse.ui.PlatformUI

class ScapsWorkingSetPage extends JavaWorkingSetPage {

  override def createControl(composite: Composite): Unit = {
    super.createControl(composite)

    // get the working set name text control and disable it
    for {
      subComposite <- composite.getChildren.collect { case c: Composite => c }.headOption
      workingSetNameText <- subComposite.getChildren.collect { case t: Text => t }.headOption
    } yield {
      workingSetNameText.setEditable(false)
      workingSetNameText.setEnabled(false)
    }
  }

  override def configureTree(tree: TreeViewer): Unit = {
    super.configureTree(tree)
    tree.setContentProvider(new ScapsWorkingSetPageContentProvider())
  }

  override def finish(): Unit = {
    super.finish
    IndexUCHandler().runIndexer(PlatformUI.getWorkbench.getActiveWorkbenchWindow)
  }

}
