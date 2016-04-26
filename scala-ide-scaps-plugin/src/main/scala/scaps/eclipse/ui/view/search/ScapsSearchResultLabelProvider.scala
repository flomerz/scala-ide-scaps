package scaps.eclipse.ui.view.search;

import org.eclipse.jface.resource.JFaceResources
import org.eclipse.jface.viewers.StyledCellLabelProvider
import org.eclipse.jface.viewers.StyledString
import org.eclipse.jface.viewers.ViewerCell
import org.eclipse.swt.graphics.RGB
import com.typesafe.scalalogging.StrictLogging
import org.scalaide.ui.ScalaImages
import org.eclipse.swt.graphics.Point
import scaps.api.Result
import scaps.api.ValueDef

/**
 * Responsible for telling Eclipse how to render the results in the
 * tree view (i.e. the view that shows the results).
 */
class ScapsSearchResultLabelProvider extends StyledCellLabelProvider with StrictLogging {

  private final val HIGHLIGHT_COLOR_NAME = "org.scala.tools.eclipse.search"
  JFaceResources.getColorRegistry().put(HIGHLIGHT_COLOR_NAME, new RGB(206, 204, 247));

  override def getToolTipShift(obj: Any): Point = new Point(5, 5)

  override def getToolTipDisplayDelayTime(obj: Any): Int = 100

  override def getToolTipTimeDisplayed(obj: Any): Int = 5000

  override def getToolTipText(element: Any): String = {
    element match {
      case result: Result[ValueDef] =>
        result.explanation.get
      case _ => ""
    }
  }

  override def update(cell: ViewerCell): Unit = {
    val text = new StyledString

    cell.getElement match {
      case result: Result[ValueDef] =>
        cell.setImage(ScalaImages.SCALA_FILE.createImage)
        text.append(result.entity.name)
        text append " - "
        text.append(result.entity.docLink.get.dropWhile(_ != '('), StyledString.COUNTER_STYLER)
        text append " - "
        text.append("Score: " + result.score.toString, StyledString.DECORATIONS_STYLER)
        text append " - "
        text.append(result.entity.module.toString, StyledString.QUALIFIER_STYLER)
      case _ =>
    }

    cell.setText(text.toString)
    cell.setStyleRanges(text.getStyleRanges)
    super.update(cell)
  }

}
