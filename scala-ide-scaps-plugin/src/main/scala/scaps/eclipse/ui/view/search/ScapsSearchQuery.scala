package scaps.eclipse.ui.view.search

import org.eclipse.search.ui.ISearchQuery
import org.eclipse.search.ui.ISearchResult
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.Status
import scaps.eclipse.ui.handlers.SearchUCHandler

class ScapsSearchQuery(query: String) extends ISearchQuery {

  val result: ScapsSearchResult = new ScapsSearchResult(this)

  def canRerun(): Boolean = false

  def canRunInBackground(): Boolean = true

  def getLabel(): String = "Scaps Search: \"" + query + "\""

  def getSearchResult(): ISearchResult = result

  def run(monitor: IProgressMonitor): IStatus = {
    result.data = SearchUCHandler()(query)
    Status.OK_STATUS
  }

}
