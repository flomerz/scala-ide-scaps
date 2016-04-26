package scaps.eclipse.ui.view.search;

import org.eclipse.jface.viewers.IStructuredContentProvider
import com.typesafe.scalalogging.StrictLogging
import org.eclipse.jface.viewers.Viewer
import scala.collection.mutable.ArrayBuffer

/**
 * Responsible for telling Eclipse what content to show after a successful
 * search.
 */
class ScapsSearchResultContentProvider(page: ScapsSearchResultPage) extends IStructuredContentProvider with StrictLogging {

  override def getElements(inputElement: Object): Array[Object] = {
    inputElement match {
      case scapsQuery: ScapsSearchQuery =>
        scapsQuery.result.data.toArray
      case _ =>
        Array()
    }
  }

  def dispose(): Unit = {}

  def inputChanged(viewer: Viewer, oldInput: Any, newInput: Any): Unit = {}

}
