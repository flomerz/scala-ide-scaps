/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package scaps.eclipse.core.services

import scaps.eclipse.core.adapters.ScapsAdapter
import scaps.api.Result
import scaps.api.ValueDef

class ScapsSearchService(private val scapsAdapter: ScapsAdapter) {

  def apply(searchQuery: String) = scapsAdapter.search(searchQuery)

}
