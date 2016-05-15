package scaps.eclipse.ui.view.search

import java.util.HashMap

import org.eclipse.core.internal.resources.File
import org.eclipse.core.internal.resources.Workspace
import org.eclipse.core.resources.IMarker
import org.eclipse.core.resources.IResource
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Path
import org.eclipse.core.runtime.Status
import org.eclipse.jface.dialogs.PopupDialog
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport
import org.eclipse.jface.viewers.ISelectionChangedListener
import org.eclipse.jface.viewers.ITreeContentProvider
import org.eclipse.jface.viewers.OpenEvent
import org.eclipse.jface.viewers.SelectionChangedEvent
import org.eclipse.jface.viewers.TableViewer
import org.eclipse.jface.viewers.TreeViewer
import org.eclipse.jface.viewers.Viewer
import org.eclipse.search.ui.IQueryListener
import org.eclipse.search.ui.ISearchQuery
import org.eclipse.search.ui.NewSearchUI
import org.eclipse.search.ui.text.AbstractTextSearchViewPage
import org.eclipse.swt.widgets.Composite
import org.eclipse.ui.PlatformUI
import org.eclipse.ui.ide.IDE
import org.eclipse.ui.progress.UIJob

import com.typesafe.scalalogging.StrictLogging
import org.eclipse.swt.widgets.Shell
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.events.FocusAdapter
import org.eclipse.swt.widgets.Control
import org.eclipse.swt.SWT
import org.eclipse.swt.widgets.Text
import org.eclipse.swt.events.FocusEvent
import org.eclipse.swt.browser.Browser
import org.eclipse.jface.viewers.StructuredSelection
import org.eclipse.swt.graphics.Color
import scaps.api.Result
import scaps.api.ValueDef
import org.eclipse.jface.layout.GridDataFactory

class ScapsSearchResultPage extends AbstractTextSearchViewPage with StrictLogging {

  private val contentProvider = new ScapsSearchResultContentProvider(this)
  private val labelProvider = new ScapsSearchResultLabelProvider()

  private def scapsDocProvider(result: Result[ValueDef]) = {
    val backgroundColor = getControl.getDisplay.getSystemColor(SWT.COLOR_INFO_BACKGROUND)
    val foregroundColor = getControl.getDisplay.getSystemColor(SWT.COLOR_INFO_FOREGROUND);
    labelProvider.getScapsDocHTML(backgroundColor, foregroundColor)(result)
  }

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

  private class ScapsDocPopupDialog(parent: Shell, htmlText: String) extends PopupDialog(parent, PopupDialog.HOVER_SHELLSTYLE, true, false, false, false, false, null, null) {
    private lazy val gridDataFactory = GridDataFactory
      .createFrom(new GridData(GridData.BEGINNING | GridData.FILL_BOTH))
      .indent(PopupDialog.POPUP_HORIZONTALSPACING, PopupDialog.POPUP_VERTICALSPACING)
      .minSize(600, 300)

    override def createDialogArea(parent: Composite): Control = {
      val browser = new Browser(parent, SWT.NONE)
      gridDataFactory.applyTo(browser)
      browser.setText(htmlText)
      browser
    }
  }

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

    tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      def selectionChanged(event: SelectionChangedEvent): Unit = {
        for {
          selection <- Option(event.getSelection).collect { case s: StructuredSelection => s }
          result <- Option(selection.getFirstElement).collect { case r: Result[ValueDef @unchecked] => r }
        } {
          val shell = tableViewer.getControl.getShell
          val scapsDocHTML = scapsDocProvider(result)
          new ScapsDocPopupDialog(shell, scapsDocHTML).open()
        }
      }
    })
  }

  def configureTreeViewer(treeViewer: TreeViewer): Unit = {
    treeViewer.setContentProvider(treeContentProvider)
  }

  def elementsChanged(elements: Array[Object]): Unit = {}

  override protected def handleOpen(event: OpenEvent): Unit = {
    event.getSource match {
      case tableViewer: TableViewer =>
        val tableItem = tableViewer.getTable.getItem(tableViewer.getTable.getSelectionIndex)
        val resultString = tableItem.getText
        // TODO: extract path out of result
        //    val pathS = resultString.split("").apply(0)
        val path = new Path("EclipseScapsPlugin/src/TestFile/testFile.scala" /*pathS*/ )
        ResourcesPlugin.getWorkspace match {
          case container: Workspace =>
            container.newResource(path, IResource.FILE) match {
              case file: File =>
                val map = new HashMap[String, Int]()
                // TODO: extract offset from result
                val line = 42
                map.put(IMarker.LINE_NUMBER, line)
                val marker = file.createMarker(IMarker.TEXT)
                marker.setAttributes(map)
                IDE.openEditor(PlatformUI.getWorkbench.getActiveWorkbenchWindow.getActivePage, marker)
            }
        }
      case _ =>
    }
  }

}
