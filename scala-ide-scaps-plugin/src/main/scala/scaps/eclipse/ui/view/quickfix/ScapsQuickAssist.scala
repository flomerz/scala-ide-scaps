package scaps.eclipse.ui.view.quickfix

import org.eclipse.jdt.core.ICompilationUnit
import org.scalaide.core.quickassist.BasicCompletionProposal
import org.scalaide.core.quickassist.InvocationContext
import org.scalaide.core.quickassist.QuickAssist

class ScapsQuickAssist extends QuickAssist {
  override def compute(ctx: InvocationContext): Seq[BasicCompletionProposal] = {
    val cu = ctx.icu.asInstanceOf[ICompilationUnit]
    List()
  }
}
