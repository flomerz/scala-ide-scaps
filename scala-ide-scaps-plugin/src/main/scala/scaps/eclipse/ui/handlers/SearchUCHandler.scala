package scaps.eclipse.ui.handlers

import org.eclipse.search.ui.NewSearchUI
import org.eclipse.ui.IWorkbenchWindow

import scaps.eclipse.ScapsPlugin
import scaps.eclipse.core.services.ScapsSearchService
import scaps.eclipse.core.services.ScapsService
import scaps.eclipse.ui.search.ScapsSearchQuery

object SearchUCHandler {
  private def INSTANCE = new SearchUCHandler(ScapsService.createSearchService)
  def apply(): SearchUCHandler = INSTANCE
}

class SearchUCHandler(scapsSearchService: ScapsSearchService) {

  def openSearchDialog(window: IWorkbenchWindow): Unit = {
    NewSearchUI.openSearchDialog(window, ScapsPlugin.SEARCH_PAGE)
  }

  def search(query: String): Unit = {
    NewSearchUI.runQueryInBackground(new ScapsSearchQuery(query, scapsSearchService.apply))
    NewSearchUI.activateSearchResultView()
  }

}
