package scaps.eclipse.ui.view.quickfix

import scala.util.matching.Regex

import org.scalaide.core.quickassist.BasicCompletionProposal
import org.scalaide.core.quickassist.InvocationContext
import org.scalaide.core.quickassist.QuickAssist

class ScapsQuickAssist extends QuickAssist {

  val ValueNotFoundError: Regex = "not found: value (.*)".r

  override def compute(ctx: InvocationContext): Seq[BasicCompletionProposal] = {
    ctx.problemLocations.flatMap { location =>
      val possibleMatch = location.annotation.getText match {
        case ValueNotFoundError(member) =>
          List(ScapsQuickAssistProposal(member, ctx.icu, location.offset, location.length))
        case _ =>
          List.empty
      }
      //      possibleMatch.filter(_.isApplicable)
      possibleMatch
    }
  }
}
