package scaps.eclipse.core.services

import scaps.eclipse.core.adapters.ScapsAdapter
import scaps.eclipse.core.models.ResultList

class ScapsService {
  private val scapsAdapter = new ScapsAdapter("indexDir")
  private val result = new ResultList
}