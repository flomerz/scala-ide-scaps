package scaps.eclipse.ui.view.search

import org.eclipse.jface.viewers.OpenEvent
import org.eclipse.jface.viewers.StructuredViewer
import org.eclipse.jface.viewers.TableViewer
import org.eclipse.jface.viewers.TreeViewer
import org.eclipse.search.ui.text.AbstractTextSearchViewPage
import org.scalaide.ui.editor.InteractiveCompilationUnitEditor
import org.eclipse.jdt.internal.ui.search.JavaSearchResultPage
import com.typesafe.scalalogging.StrictLogging
import org.eclipse.search.ui.ISearchResult
import javax.swing.text.html.TableView
import org.eclipse.swt.widgets.Composite
import org.eclipse.search.ui.NewSearchUI
import org.eclipse.search.ui.IQueryListener
import org.eclipse.search.ui.ISearchQuery
import org.eclipse.ui.progress.UIJob
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.search.ui.text.AbstractTextSearchResult
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Status
import org.eclipse.jface.viewers.StyledCellLabelProvider
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport
import org.eclipse.jface.viewers.IContentProvider
import org.eclipse.jface.viewers.Viewer
import org.eclipse.jface.viewers.ITreeContentProvider

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

  override protected def handleOpen(event: OpenEvent): Unit = {}

}
