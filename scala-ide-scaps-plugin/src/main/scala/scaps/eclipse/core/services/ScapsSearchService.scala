package scaps.eclipse.core.services

import scaps.eclipse.core.adapters.ScapsAdapter
import scaps.api.Result
import scaps.api.ValueDef

class ScapsSearchService(private val scapsAdapter: ScapsAdapter) {

  def apply(searchQuery: String) = scapsAdapter.search(searchQuery)

}
