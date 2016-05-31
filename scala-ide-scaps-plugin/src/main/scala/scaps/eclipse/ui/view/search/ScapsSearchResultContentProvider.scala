package scaps.eclipse.ui.view.search;

import org.eclipse.jface.viewers.IStructuredContentProvider
import org.eclipse.jface.viewers.Viewer

import com.typesafe.scalalogging.StrictLogging
import scaps.eclipse.ui.search.ScapsSearchQuery

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
