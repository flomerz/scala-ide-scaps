/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package scaps.eclipse.ui.view.search;

import org.eclipse.jface.resource.JFaceResources
import org.eclipse.jface.viewers.StyledCellLabelProvider
import org.eclipse.jface.viewers.StyledString
import org.eclipse.jface.viewers.ViewerCell
import org.eclipse.swt.graphics.Color
import org.eclipse.swt.graphics.RGB
import org.scalaide.ui.ScalaImages

import com.typesafe.scalalogging.StrictLogging

import scaps.api.Result
import scaps.api.ValueDef

/**
 * Responsible for telling Eclipse how to render the results in the
 * tree view (i.e. the view that shows the results).
 */
class ScapsSearchResultLabelProvider extends StyledCellLabelProvider with StrictLogging {

  private final val HIGHLIGHT_COLOR_NAME = "org.scala.tools.eclipse.search"
  JFaceResources.getColorRegistry().put(HIGHLIGHT_COLOR_NAME, new RGB(206, 204, 247));

  private object ScapsDocGenerator {
    implicit class WebColor(color: Color) {
      val toRGBCode: String = {
        val r = color.getRed
        val g = color.getGreen
        val b = color.getBlue
        f"#$r%02X$g%02X$b%02X"
      }
    }

    private def createCSSStyle(backgroundRGBCode: String, foregroundRGBCode: String): String = s"""
        body {
          background-color: $backgroundRGBCode;
          color: $foregroundRGBCode;
          font-size: 14px;
        }
        p { margin: 0px; }
        .description { margin-top: 10px; }
        .additionals { margin-top: 20px; }
        .attribute {
            font-weight: bold;
            margin-top: 20px;
            margin-bottom: 5px;
        }
        .attributeBody dt { float: left;}
        .attributeBody dl { margin: 0px; }
      """

    private def createHTML(cssStyle: String, body: String): String =
      s"""<html><head><style>$cssStyle</style></head><body>$body</body></html>"""

    private def createBody(description: String, attributes: String): String =
      s"""<div class="description">$description</div>$attributes"""

    private def createAttributes(attributes: Seq[(String, String)]): String = attributes.map {
      case (attribute, body) => s"""<div class="attribute">$attribute</div><div class="attributeBody">$body</div>"""
    }.mkString

    private def createAdditionals(score: Float, artifactPath: String): String =
      s"""<div class="additionals">Source:</br>$artifactPath</div>
          <div class="additionals">Score: $score</div>"""

    def apply(backgroundColor: Color, foregroundColor: Color, result: Result[ValueDef]): String = {
      val comment = result.entity.comment
      createHTML(
        createCSSStyle(backgroundColor.toRGBCode, foregroundColor.toRGBCode),
        createBody(
          comment.body,
          createAttributes(comment.attributes))
          + createAdditionals(result.score, result.entity.source.artifactPath))
    }
  }

  def getScapsDocHTML(backgroundColor: Color, foregroundColor: Color)(result: Result[ValueDef]): String =
    ScapsDocGenerator(backgroundColor, foregroundColor, result)

  override def update(cell: ViewerCell): Unit = {
    val text = new StyledString

    cell.getElement match {
      case result @ Result(entity: ValueDef, _, _) =>
        cell.setImage(ScalaImages.SCALA_FILE.createImage)
        text.append(entity.name)
        text.append(entity.docLink.get.dropWhile(_ != '('), StyledString.COUNTER_STYLER)
      case _ =>
    }

    cell.setText(text.toString)
    cell.setStyleRanges(text.getStyleRanges)
    super.update(cell)
  }

}
