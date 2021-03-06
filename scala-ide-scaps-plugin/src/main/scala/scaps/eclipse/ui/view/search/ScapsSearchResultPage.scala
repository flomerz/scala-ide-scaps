/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package scaps.eclipse.ui.view.search

import java.net.URI
import java.util.zip.ZipFile

import org.eclipse.core.filesystem.EFS
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Path
import org.eclipse.core.runtime.Status
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.internal.core.JarEntryFile
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
import scaps.api.ValueDef
import scaps.eclipse.ui.search.ScapsSearchQuery
import org.eclipse.jdt.internal.core.JavaProject
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot
import org.eclipse.jdt.ui.JavaUI
import org.eclipse.ui.IEditorPart
import scaps.api.Source
import scaps.eclipse.ui.handlers.SearchUCHandler

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
      SearchUCHandler().openSource(valueDef.source)
    }
  }

  def configureTreeViewer(treeViewer: TreeViewer): Unit = {}

  def elementsChanged(elements: Array[Object]): Unit = {}

}
