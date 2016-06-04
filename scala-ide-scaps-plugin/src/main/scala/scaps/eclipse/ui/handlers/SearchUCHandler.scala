package scaps.eclipse.ui.handlers

import org.eclipse.search.ui.NewSearchUI
import org.eclipse.ui.IWorkbenchWindow

import scalaz.{ -\/ => -\/ }
import scalaz.{ \/- => \/- }
import scaps.eclipse.ScapsPlugin
import scaps.eclipse.core.services.ScapsSearchService
import scaps.eclipse.core.services.ScapsService
import scaps.eclipse.ui.search.ScapsSearchQuery
import scaps.eclipse.util.ErrorHandler
import scaps.eclipse.core.services.ScapsSettingsService
import scaps.eclipse.ui.handlers.util.OpenSourceHelper
import scaps.api.Source

object SearchUCHandler {
  private def INSTANCE = new SearchUCHandler(ScapsService.createSearchService)
  def apply(): SearchUCHandler = INSTANCE
}

class SearchUCHandler(scapsSearchService: ScapsSearchService) {

  def openSearchDialog(window: IWorkbenchWindow): Unit = {
    NewSearchUI.openSearchDialog(window, ScapsPlugin.SEARCH_PAGE)
  }

  def openSearchDialog(window: IWorkbenchWindow, query: String): Unit = {
    ScapsSettingsService.setPrefilledSearchQuery(query)
    openSearchDialog(window)
  }

  def getPrefilledSearchQuery = ScapsSettingsService.getPrefilledSearchQuery

  def search(query: String): Unit = {
    def searchInternal(query: String) = scapsSearchService(query) match {
      case -\/(error) => {
        ErrorHandler(error)
        Seq.empty
      }
      case \/-(result) => result
    }

    ScapsSettingsService.setPrefilledSearchQuery(query)
    NewSearchUI.runQueryInBackground(new ScapsSearchQuery(query, searchInternal))
    NewSearchUI.activateSearchResultView()
  }

  def openSource(source: Source): Unit = OpenSourceHelper(source)

}
