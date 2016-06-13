/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package scaps.eclipse.ui.search

import org.eclipse.jface.resource.ImageDescriptor
import org.eclipse.search.ui.ISearchQuery
import org.eclipse.search.ui.ISearchResult
import org.eclipse.search.ui.ISearchResultListener
import org.scalaide.ui.ScalaImages

import scaps.api.Result
import scaps.api.ValueDef

/**
 * Represents the result of executing a search query against Scala
 * files.
 */
class ScapsSearchResult(query: ScapsSearchQuery) extends ISearchResult {

  class ResultWrapper(r: Result[ValueDef]) extends Result[ValueDef](r.entity, r.score, r.explanation) {
    override val entity = r.entity
    override val score = r.score
    override val explanation = r.explanation

    override def toString = name + typeDef

    val name = r.entity.name
    val typeDef = entity.docLink.get.dropWhile(_ != '(')
  }

  private var data: Seq[Result[ValueDef]] = _

  def setData(data: Seq[Result[ValueDef]]): Unit = this.data = data.map(new ResultWrapper(_))
  def getData(): Seq[Result[ValueDef]] = data

  /**
   * The image descriptor for the given ISearchResult.
   */
  def getImageDescriptor(): ImageDescriptor = ScalaImages.SCALA_FILE

  /**
   * A user readable label for this search result. The label is typically used in the result view
   * and should contain the search query string and number of matches.
   */
  def getLabel(): String = query.getLabel

  /**
   * The query that produced this search result.
   */
  def getQuery(): ISearchQuery = query

  /**
   * A tooltip to be used when this search result is shown in the UI.
   */
  def getTooltip(): String = query.getLabel

  def addListener(l: ISearchResultListener): Unit = {}

  def removeListener(l: ISearchResultListener): Unit = {}

}
