package scaps.eclipse.ui.handlers

import scala.util.Try

import org.eclipse.search.ui.NewSearchUI
import org.eclipse.ui.IWorkbenchWindow

import scalaz.\/
import scalaz.-\/
import scalaz.\/-
import scaps.api.Result
import scaps.api.ValueDef
import scaps.eclipse.ScapsPlugin
import scaps.eclipse.core.adapters.ScapsEngineError
import scaps.eclipse.core.services.ScapsSearchService
import scaps.eclipse.core.services.ScapsService
import scaps.eclipse.ui.search.ScapsSearchQuery
import scaps.searchEngine.QueryError
import scala.util.Success
import scala.util.Failure

object SearchUCHandler {
  private def INSTANCE = new SearchUCHandler(ScapsService.createSearchService)
  def apply(): SearchUCHandler = INSTANCE
}

class SearchUCHandler(scapsSearchService: ScapsSearchService) {

  def openSearchDialog(window: IWorkbenchWindow): Unit = {
    NewSearchUI.openSearchDialog(window, ScapsPlugin.SEARCH_PAGE)
  }

  def search(query: String): Unit = {
    def searchInternal(query: String) = scapsSearchService(query) match {
      case -\/(error) => {
        ErrorUCHandler(error)
        Seq.empty
      }
      case \/-(result) => result
    }

    NewSearchUI.runQueryInBackground(new ScapsSearchQuery(query, searchInternal))
    NewSearchUI.activateSearchResultView()
  }

}
