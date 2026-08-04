package GROUP_ID.designsystem.detekt

import io.gitlab.arturbosch.detekt.api.*
import org.jetbrains.kotlin.psi.*

/**
 * Flags direct TextStyle(...) construction outside of token files.
 *
 * The design system exposes typography through the AppTextStyle enum and
 * appTheme.typography.*. Constructing TextStyle inline bypasses the token
 * system and creates one-off values that won't adapt to dark mode or brand updates.
 */
class DirectTextStyleRule(config: Config) : Rule(config) {

    override val issue = Issue(
        id = "DirectTextStyle",
        severity = Severity.Error,
        description = "Direct TextStyle(...) construction. Use AppTextStyle enum via appTheme.typography.* instead.",
        debt = Debt.FIVE_MINS,
    )

    private val skipSuffixes = setOf("Typography.kt", "Styles.kt", "Tokens.kt", "Theme.kt")

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)

        if (expression.calleeExpression?.text != "TextStyle") return

        val filePath = expression.containingKtFile.virtualFilePath
        if (skipSuffixes.any { filePath.endsWith(it) }) return
        if (filePath.contains("/theme/") || filePath.contains("/tokens/")) return

        report(CodeSmell(issue, Entity.from(expression),
            "TextStyle(...) construction is a design system bypass. " +
            "Use AppTextStyle.* passed through appTheme.typography.bodyLarge (or similar)."))
    }
}
