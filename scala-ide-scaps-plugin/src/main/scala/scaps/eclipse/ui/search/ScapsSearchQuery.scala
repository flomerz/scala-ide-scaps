/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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
