package scaps.eclipse.ui.view.search

import java.util.HashMap

import org.eclipse.core.resources.IMarker
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Path
import org.eclipse.core.runtime.Status
import org.eclipse.jface.dialogs.PopupDialog
import org.eclipse.jface.layout.GridDataFactory
import org.eclipse.jface.viewers.ISelectionChangedListener
import org.eclipse.jface.viewers.ITreeContentProvider
import org.eclipse.jface.viewers.OpenEvent
import org.eclipse.jface.viewers.SelectionChangedEvent
import org.eclipse.jface.viewers.StructuredSelection
import org.eclipse.jface.viewers.TableViewer
import org.eclipse.jface.viewers.TreeViewer
import org.eclipse.jface.viewers.Viewer
import org.eclipse.search.ui.IQueryListener
import org.eclipse.search.ui.ISearchQuery
import org.eclipse.search.ui.NewSearchUI
import org.eclipse.search.ui.text.AbstractTextSearchViewPage
import org.eclipse.swt.SWT
import org.eclipse.swt.browser.Browser
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Control
import org.eclipse.swt.widgets.Shell
import org.eclipse.ui.PlatformUI
import org.eclipse.ui.ide.IDE
import org.eclipse.ui.progress.UIJob

import com.typesafe.scalalogging.StrictLogging

import scaps.api.FileSource
import scaps.api.Result
import scaps.api.ValueDef
import org.eclipse.swt.custom.SashForm
import org.eclipse.swt.widgets.Text
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.custom.SashFormData

class ScapsSearchResultPage extends AbstractTextSearchViewPage(AbstractTextSearchViewPage.FLAG_LAYOUT_FLAT) with StrictLogging {

  private var scapsDocBrowser: Browser = _

  private val contentProvider = new ScapsSearchResultContentProvider(this)
  private val labelProvider = new ScapsSearchResultLabelProvider()

  private lazy val scapsDocProvider = {
    val backgroundColor = getControl.getDisplay.getSystemColor(SWT.COLOR_INFO_BACKGROUND)
    val foregroundColor = getControl.getDisplay.getSystemColor(SWT.COLOR_INFO_FOREGROUND)
    labelProvider.getScapsDocHTML(backgroundColor, foregroundColor)(_)
  }

  // Könnte man in eigene Klasse extrahieren, sieht ein bisschen hässlich aus hier:
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
    def queryFinished(query: ISearchQuery): Unit = query match {
      case scapsQuery: ScapsSearchQuery =>
        logger.info("query finished - " + scapsQuery.getLabel)
        updateUI(scapsQuery)
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

  override def createControl(parent: Composite): Unit = {
    super.createControl(parent)
    NewSearchUI.addQueryListener(scapsQueryListener)
  }

  override def createTableViewer(parent: Composite): TableViewer = {
    val sashForm = new SashForm(parent, SWT.HORIZONTAL)
    sashForm.setSashWidth(4)
    sashForm.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_GRAY))
    val tableViewer = super.createTableViewer(sashForm)
    scapsDocBrowser = new Browser(sashForm, SWT.NONE)
    sashForm.setWeights(Array(70, 30))
    tableViewer
  }

  def clear(): Unit = {}

  def configureTableViewer(tableViewer: TableViewer): Unit = {
    logger.info("configure search result table viewer")
    tableViewer.setContentProvider(contentProvider)
    tableViewer.setLabelProvider(labelProvider)

    tableViewer.addSelectionChangedListener(new ISelectionChangedListener {
      def selectionChanged(event: SelectionChangedEvent): Unit = {
        for {
          selection <- Option(event.getSelection).collect { case s: StructuredSelection => s }
          /* Ich habe das mal refactored um das @unchecked weg zu kriegen,
           * alelrdings ist der Code sehr unschön. Allerdings wird später im
           * scapsDocProvider soweit ich sehe nur die entity gebraucht, man
           * könnte also stattdessen nur das übergeben, das ginge dann so:
           *
           *  collect { case Result(v: ValueDef, _, _) => v }
           *
           * und sieht wieder hübscher aus.
           *  */
          result <- Option(selection.getFirstElement).collect { case r @ Result(_: ValueDef, _, _) => r.asInstanceOf[Result[ValueDef]] }
        } {
          val scapsDocHTML = scapsDocProvider(result)
          scapsDocBrowser.setText(scapsDocHTML)
        }
      }
    })
  }

  def configureTreeViewer(treeViewer: TreeViewer): Unit = {
    treeViewer.setContentProvider(treeContentProvider)
  }

  def elementsChanged(elements: Array[Object]): Unit = {}

  override protected def handleOpen(event: OpenEvent): Unit = {
    event.getSelection match {
      case selection: StructuredSelection =>
        selection.getFirstElement match {
          // TODO: cover other cases like jar
          case result: Result[ValueDef @unchecked] =>
            result.entity.source match {
              case fileSource: FileSource =>
                val artifactPath = fileSource.artifactPath
                val relativePath = artifactPath.substring(ResourcesPlugin.getWorkspace.getRoot.getLocation.toString.length)
                val path = new Path(relativePath)
                val file = ResourcesPlugin.getWorkspace.getRoot.getFile(path)

                val map = new HashMap[String, Int]()
                // TODO: extract offset from result
                val line = 42
                map.put(IMarker.LINE_NUMBER, line)
                val marker = file.createMarker(IMarker.TEXT)
                marker.setAttributes(map)
                IDE.openEditor(PlatformUI.getWorkbench.getActiveWorkbenchWindow.getActivePage, marker)
            }
          case _ =>
        }
      case _ =>
    }
  }

}
