package scaps.eclipse.ui.handlers

import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jdt.core.IJavaProject
import scaps.eclipse.core.services.ScapsService
import org.eclipse.jdt.core.IPackageFragmentRoot
import scaps.api.Result
import scaps.api.ValueDef
import scaps.eclipse.core.services.ScapsSearchService

object SearchUCHandler {
  private def INSTANCE = new SearchUCHandler(ScapsService.createSearchService)
  def apply(): SearchUCHandler = INSTANCE
}

class SearchUCHandler(scapsSearchService: ScapsSearchService) {

  def apply(searchQuery: String): Seq[Result[ValueDef]] = scapsSearchService(searchQuery)

}
