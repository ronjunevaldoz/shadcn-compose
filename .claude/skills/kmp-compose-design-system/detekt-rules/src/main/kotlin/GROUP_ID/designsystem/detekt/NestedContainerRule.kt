package GROUP_ID.designsystem.detekt

import io.gitlab.arturbosch.detekt.api.*
import org.jetbrains.kotlin.psi.*

/**
 * Detects nested Card or Surface calls at the top level of a lambda body.
 *
 * Uses PSI traversal instead of regex brace-counting, so it correctly handles:
 * - Trailing-lambda syntax:  Card { Surface { ... } }
 * - Named parameter syntax:  Card(modifier = Modifier) { Surface { ... } }
 * - Conditional wrapping is NOT flagged (the inner call must be the direct child)
 *
 * Nested containers create double elevation, double background fill, and
 * double accessibility semantics on Android.
 */
class NestedContainerRule(config: Config) : Rule(config) {

    override val issue = Issue(
        id = "NestedContainer",
        severity = Severity.Warning,
        description = "Nested Card/Surface found. Remove the outer wrapper to eliminate double elevation.",
        debt = Debt.TEN_MINS,
    )

    private val containers = setOf("Card", "Surface")

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)

        val outerName = expression.calleeExpression?.text ?: return
        if (outerName !in containers) return

        // Resolve the trailing lambda or last lambda argument
        val lambda = expression.lambdaArguments.firstOrNull()?.getLambdaExpression()
            ?: (expression.valueArguments.lastOrNull()?.getArgumentExpression() as? KtLambdaExpression)
            ?: return

        val body = lambda.bodyExpression ?: return

        // Walk the direct statements in the lambda body
        for (stmt in body.statements) {
            val innerCall = extractCallExpression(stmt) ?: continue
            val innerName = innerCall.calleeExpression?.text ?: continue
            if (innerName in containers) {
                report(CodeSmell(issue, Entity.from(expression),
                    "$outerName { $innerName { … } } — nested containers add double elevation. " +
                    "Keep only the innermost $innerName."))
                return  // Report once per outer container
            }
        }
    }

    private fun extractCallExpression(element: KtElement): KtCallExpression? = when (element) {
        is KtCallExpression -> element
        is KtDotQualifiedExpression -> element.selectorExpression as? KtCallExpression
        is KtBlockExpression -> element.statements.firstOrNull()?.let { extractCallExpression(it) }
        else -> null
    }
}
