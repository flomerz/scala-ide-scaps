/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package scaps.eclipse.ui.view.search;

import org.eclipse.jface.viewers.IStructuredContentProvider
import org.eclipse.jface.viewers.Viewer

import com.typesafe.scalalogging.StrictLogging
import scaps.eclipse.ui.search.ScapsSearchQuery
import scaps.api.Result
import scaps.api.ValueDef

/**
 * Responsible for telling Eclipse what content to show after a successful
 * search.
 */
class ScapsSearchResultContentProvider(page: ScapsSearchResultPage) extends IStructuredContentProvider with StrictLogging {

  override def getElements(inputElement: Object): Array[Object] = {
    inputElement match {
      case scapsQuery: ScapsSearchQuery =>
        scapsQuery.result.getData.toArray
      case _ =>
        Array()
    }
  }

  def dispose(): Unit = {}

  def inputChanged(viewer: Viewer, oldInput: Any, newInput: Any): Unit = {}

}
