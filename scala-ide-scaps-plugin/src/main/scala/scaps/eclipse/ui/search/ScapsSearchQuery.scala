package scaps.eclipse.ui.search

import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Status
import org.eclipse.search.ui.ISearchQuery
import org.eclipse.search.ui.ISearchResult

import scaps.api.Result
import scaps.api.ValueDef

class ScapsSearchQuery(query: String, searchFunction: (String) => Seq[Result[ValueDef]]) extends ISearchQuery {

  val result: ScapsSearchResult = new ScapsSearchResult(this)

  def canRerun(): Boolean = false

  def canRunInBackground(): Boolean = true

  def getLabel(): String = "Scaps Search: \"" + query + "\""

  def getSearchResult(): ISearchResult = result

  def run(monitor: IProgressMonitor): IStatus = {
    result.setData(searchFunction(query))
    Status.OK_STATUS
  }

}
