package org.scalaide.scaps.core.services

import org.scalaide.scaps.core.adapters.ScapsAdapter
import org.scalaide.scaps.core.models.ResultList

class ScapsService {
  private val scapsAdapter = new ScapsAdapter
  private val result = new ResultList
}