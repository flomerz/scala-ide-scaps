package scaps.eclipse.ui.view.search;

import org.eclipse.jface.resource.ImageDescriptor
import org.eclipse.search.ui.ISearchQuery
import org.eclipse.search.ui.text.AbstractTextSearchResult
import org.scalaide.ui.ScalaImages
import org.eclipse.search.ui.ISearchResultListener
import org.eclipse.jdt.internal.ui.search.AbstractJavaSearchResult
import org.eclipse.search.ui.ISearchResult
import scaps.api.Result
import scaps.api.ValueDef

/**
 * Represents the result of executing a search query against Scala
 * files.
 */
class ScapsSearchResult(query: ScapsSearchQuery) extends ISearchResult {

  private var data: Seq[Result[ValueDef]] = _

  def setData(data: Seq[Result[ValueDef]]): Unit = this.data = data
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
