package GROUP_ID.designsystem.detekt

import io.gitlab.arturbosch.detekt.api.*
import org.jetbrains.kotlin.psi.*

/**
 * Flags any access of MaterialTheme.colors, MaterialTheme.colorScheme,
 * MaterialTheme.typography, or MaterialTheme.shapes.
 *
 * These are compile errors when the design system does not depend on Material3.
 * Even in projects that do have Material3, using MaterialTheme alongside AppTheme
 * creates two competing token systems.
 */
class MaterialThemeUsageRule(config: Config) : Rule(config) {

    override val issue = Issue(
        id = "MaterialThemeUsage",
        severity = Severity.Error,
        description = "MaterialTheme.* access found. Use appTheme.* from the design system instead.",
        debt = Debt.TEN_MINS,
    )

    private val materialProperties = setOf(
        "colors", "colorScheme", "typography", "shapes", "textSelectionColors",
    )

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)

        val receiver = expression.receiverExpression.text
        val selector = expression.selectorExpression?.text ?: return

        if (receiver == "MaterialTheme" && selector in materialProperties) {
            report(CodeSmell(issue, Entity.from(expression),
                "MaterialTheme.$selector — replace with the design system equivalent: " +
                "appTheme.${selector.removePrefix("colorScheme").ifEmpty { "colors" }}.*"))
        }
    }
}
