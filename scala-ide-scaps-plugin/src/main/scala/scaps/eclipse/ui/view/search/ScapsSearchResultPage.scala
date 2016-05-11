package scaps.eclipse.ui.view.search

import org.eclipse.core.internal.resources.File
import org.eclipse.core.internal.resources.Workspace
import org.eclipse.core.resources.IResource
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Path
import org.eclipse.core.runtime.Status
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport
import org.eclipse.jface.viewers.IStructuredSelection
import org.eclipse.jface.viewers.ITreeContentProvider
import org.eclipse.jface.viewers.OpenEvent
import org.eclipse.jface.viewers.TableViewer
import org.eclipse.jface.viewers.TreeViewer
import org.eclipse.jface.viewers.Viewer
import org.eclipse.search.ui.IQueryListener
import org.eclipse.search.ui.ISearchQuery
import org.eclipse.search.ui.NewSearchUI
import org.eclipse.search.ui.text.AbstractTextSearchViewPage
import org.eclipse.swt.widgets.Composite
import org.eclipse.ui.progress.UIJob

import com.typesafe.scalalogging.StrictLogging

class ScapsSearchResultPage extends AbstractTextSearchViewPage with StrictLogging {

  private val contentProvider = new ScapsSearchResultContentProvider(this)
  private val labelProvider = new ScapsSearchResultLabelProvider()

  private val treeContentProvider = new ITreeContentProvider() {
    def dispose(): Unit = {}
    def inputChanged(viewer: Viewer, x: Any, y: Any): Unit = {}
    def getChildren(x$1: Any): Array[Object] = Array()
    def getElements(x$1: Any): Array[Object] = Array()
    def getParent(x$1: Any): Object = null
    def hasChildren(x$1: Any): Boolean = false
  }

  private val scapsQueryListener = new IQueryListener() {
    def queryAdded(query: ISearchQuery): Unit = {}
    def queryRemoved(query: ISearchQuery): Unit = {}
    def queryStarting(query: ISearchQuery): Unit = {}
    def queryFinished(query: ISearchQuery): Unit = {
      query match {
        case scapsQuery: ScapsSearchQuery =>
          logger.info("query finished - " + scapsQuery.getLabel)
          updateUI(scapsQuery)
      }
    }
  }

  private def updateUI(query: ScapsSearchQuery) = new UIJob("Refresh Scaps Search Results") {
    def runInUIThread(monitor: IProgressMonitor): IStatus = {
      if (getViewer.getContentProvider != null) {
        getViewer.setInput(query)
      }
      Status.OK_STATUS
    }
  }.schedule

  override def createControl(composite: Composite): Unit = {
    super.createControl(composite)
    setLayout(AbstractTextSearchViewPage.FLAG_LAYOUT_FLAT)
    NewSearchUI.addQueryListener(scapsQueryListener)
  }

  def clear(): Unit = {}

  def configureTableViewer(tableViewer: TableViewer): Unit = {
    logger.info("configure search result table viewer")
    tableViewer.setContentProvider(contentProvider)
    tableViewer.setLabelProvider(labelProvider)
    ColumnViewerToolTipSupport.enableFor(tableViewer)
  }

  def configureTreeViewer(treeViewer: TreeViewer): Unit = {
    treeViewer.setContentProvider(treeContentProvider)
  }

  def elementsChanged(elements: Array[Object]): Unit = {}

  override protected def handleOpen(event: OpenEvent): Unit = {
    val selection: IStructuredSelection = event.getSelection.asInstanceOf[IStructuredSelection]
    val tableviewer: TableViewer = event.getSource().asInstanceOf[TableViewer]
    val tableItem = tableviewer.getTable.getItem(tableviewer.getTable.getSelectionIndex)
    val resultString = tableItem.getText
    // TODO: extract path out of result
    //    val pathS = resultString.split("").apply(0)
    val path = new Path("EclipseScapsPlugin/src/TestFile/testFile.scala" /*pathS*/ )
    val container: Workspace = ResourcesPlugin.getWorkspace.asInstanceOf[Workspace]
    val gk = container.newResource(path, IResource.FILE).asInstanceOf[File]
    val editor = EditorUtility.openInEditor(gk, true)
  }

}
