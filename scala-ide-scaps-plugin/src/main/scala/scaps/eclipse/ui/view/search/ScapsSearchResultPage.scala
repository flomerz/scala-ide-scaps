package scaps.eclipse.ui.view.search

import org.eclipse.core.filesystem.EFS
import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Path
import org.eclipse.core.runtime.Status
import org.eclipse.jface.viewers.ISelectionChangedListener
import org.eclipse.jface.viewers.OpenEvent
import org.eclipse.jface.viewers.SelectionChangedEvent
import org.eclipse.jface.viewers.StructuredSelection
import org.eclipse.jface.viewers.TableViewer
import org.eclipse.jface.viewers.TreeViewer
import org.eclipse.search.ui.IQueryListener
import org.eclipse.search.ui.ISearchQuery
import org.eclipse.search.ui.NewSearchUI
import org.eclipse.search.ui.text.AbstractTextSearchViewPage
import org.eclipse.swt.SWT
import org.eclipse.swt.browser.Browser
import org.eclipse.swt.custom.SashForm
import org.eclipse.swt.widgets.Composite
import org.eclipse.ui.PlatformUI
import org.eclipse.ui.ide.IDE
import org.eclipse.ui.progress.UIJob
import org.eclipse.ui.texteditor.ITextEditor

import com.typesafe.scalalogging.StrictLogging

import scaps.api.FileSource
import scaps.api.PosSource
import scaps.api.Result
import scaps.api.Source
import scaps.api.ValueDef
import scaps.eclipse.ui.search.ScapsSearchQuery
import java.util.zip.ZipFile

class ScapsSearchResultPage extends AbstractTextSearchViewPage(AbstractTextSearchViewPage.FLAG_LAYOUT_FLAT) with StrictLogging {

  private var scapsDocBrowser: Browser = _

  private val contentProvider = new ScapsSearchResultContentProvider(this)
  private val labelProvider = new ScapsSearchResultLabelProvider()

  private lazy val scapsDocProvider = {
    val backgroundColor = getControl.getDisplay.getSystemColor(SWT.COLOR_INFO_BACKGROUND)
    val foregroundColor = getControl.getDisplay.getSystemColor(SWT.COLOR_INFO_FOREGROUND)
    labelProvider.getScapsDocHTML(backgroundColor, foregroundColor)(_)
  }

  private class ScapsQueryListener extends IQueryListener {
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
    NewSearchUI.addQueryListener(new ScapsQueryListener)
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
          result <- Option(selection.getFirstElement).collect { case r @ Result(_: ValueDef, _, _) => r.asInstanceOf[Result[ValueDef]] }
        } {
          val scapsDocHTML = scapsDocProvider(result)
          scapsDocBrowser.setText(scapsDocHTML)
        }
      }
    })
  }

  override protected def handleOpen(event: OpenEvent): Unit = {
    for {
      selection <- Option(event.getSelection).collect { case s: StructuredSelection => s }
      valueDef <- Option(selection.getFirstElement).collect { case Result(valueDef: ValueDef, _, _) => valueDef }
    } {
      valueDef.source match {
        case fileSource @ FileSource(_, _: PosSource) =>
          val path = new Path(fileSource.artifactPath)
          openFileInEditor(path, fileSource.startPos.getOrElse(0), fileSource.endPos.getOrElse(0))
        case fileSource @ FileSource(_, fileInJarSource @ FileSource(_, _: PosSource)) =>
          val path = new Path(fileSource.artifactPath)
          println("+++++++ LIB +++++")
          println(path)
          //          x(path, fileInJarSource.artifactPath)
          openFileInEditor(path, 0, 0)
        case _ =>
      }
    }
  }

  def x(path: IPath, innerSource: String): Unit = {
    val fileStore = EFS.getLocalFileSystem.getStore(path)
    val file = fileStore.toLocalFile(EFS.NONE, null)
    val zip = new ZipFile(file)
    val x = zip.getEntry(innerSource)
    val editor = IDE.openEditorOnFileStore(PlatformUI.getWorkbench.getActiveWorkbenchWindow.getActivePage, fileStore)
    editor match {
      case textEditor: ITextEditor => selectLineInEditor(5, 8, textEditor)
      case _                       =>
    }
  }

  def openFileInEditor(path: IPath, startPos: Int, endPos: Int): Unit = {
    val fileStore = EFS.getLocalFileSystem.getStore(path)
    val editor = IDE.openEditorOnFileStore(PlatformUI.getWorkbench.getActiveWorkbenchWindow.getActivePage, fileStore)
    editor match {
      case textEditor: ITextEditor => selectLineInEditor(startPos, endPos, textEditor)
      case _                       =>
    }
  }

  def selectLineInEditor(startPos: Int, endPos: Int, editor: ITextEditor): Unit = {
    for {
      documentProvider <- Option(editor.getDocumentProvider)
      editorInput <- Option(editor.getEditorInput)
      document <- Option(documentProvider.getDocument(editorInput))
    } {
      editor.selectAndReveal(startPos, endPos - startPos)
    }
  }

  def configureTreeViewer(treeViewer: TreeViewer): Unit = {}

  def elementsChanged(elements: Array[Object]): Unit = {}

}
