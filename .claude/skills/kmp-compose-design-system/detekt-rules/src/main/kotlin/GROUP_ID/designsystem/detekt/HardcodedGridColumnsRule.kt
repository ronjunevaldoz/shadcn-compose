package GROUP_ID.designsystem.detekt

import io.gitlab.arturbosch.detekt.api.*
import org.jetbrains.kotlin.psi.*

/**
 * Flags LazyVerticalGrid / LazyHorizontalGrid with GridCells.Fixed(N) where N >= 2.
 *
 * Fixed column counts break on smaller screens: a 2-column grid with 1 item
 * leaves half the screen empty on phone, and a 3-column grid becomes unreadable
 * on compact width.
 *
 * Preferred alternatives:
 *   GridCells.Adaptive(minSize = 160.dp)           — auto-calculates columns from width
 *   if (windowSizeClass == Compact) Fixed(1) else Fixed(2)  — explicit WindowSizeClass guard
 *
 * GridCells.Fixed(1) is exempt — single-column lists are always correct.
 */
class HardcodedGridColumnsRule(config: Config) : Rule(config) {

    override val issue = Issue(
        id = "HardcodedGridColumns",
        severity = Severity.Warning,
        description = "GridCells.Fixed(N≥2) found. Use GridCells.Adaptive(minSize) or a WindowSizeClass guard instead.",
        debt = Debt.FIVE_MINS,
    )

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)

        // Match GridCells.Fixed(N)
        val receiver = expression.receiverExpression.text
        if (receiver != "GridCells") return

        val selectorCall = expression.selectorExpression as? KtCallExpression ?: return
        if (selectorCall.calleeExpression?.text != "Fixed") return

        val arg = selectorCall.valueArguments.firstOrNull()
            ?.getArgumentExpression()?.text ?: return
        val n = arg.trimEnd('L', 'l').toIntOrNull() ?: return
        if (n < 2) return  // Fixed(1) is fine

        report(CodeSmell(issue, Entity.from(expression),
            "GridCells.Fixed($n) is hardcoded. On compact screens this may render " +
            "${n - 1} empty column(s) when the list has fewer items than columns. " +
            "Use GridCells.Adaptive(minSize = Xdp) or check windowSizeClass before choosing Fixed($n)."))
    }
}
