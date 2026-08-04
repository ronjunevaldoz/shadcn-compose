package GROUP_ID.designsystem.detekt

import io.gitlab.arturbosch.detekt.api.*
import org.jetbrains.kotlin.psi.*

/**
 * Flags Color() literals constructed with raw hex integers or float RGB values.
 * Exempts token definition files (Colors.kt, Tokens.kt, Theme.kt, etc.) and
 * any file under tokens/ or theme/ directories.
 */
class HardcodedColorRule(config: Config) : Rule(config) {

    override val issue = Issue(
        id = "HardcodedColor",
        severity = Severity.Error,
        description = "Hardcoded Color() literal. Use appTheme.colors.* instead.",
        debt = Debt.FIVE_MINS,
    )

    private val skipSuffixes = setOf(
        "Colors.kt", "Tokens.kt", "Theme.kt", "Styles.kt",
        "Typography.kt", "Spacing.kt", "Shapes.kt",
    )

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)

        if (expression.calleeExpression?.text != "Color") return

        val filePath = expression.containingKtFile.virtualFilePath
        if (skipSuffixes.any { filePath.endsWith(it) }) return
        if (filePath.contains("/theme/") || filePath.contains("/tokens/")) return

        val args = expression.valueArguments
        when (args.size) {
            // Color(0xFFRRGGBB) or Color(0xAARRGGBB) — hex/int literal
            1 -> {
                val text = args[0].getArgumentExpression()?.text ?: return
                if (text.startsWith("0x", ignoreCase = true) ||
                    text.all { it.isDigit() || it == 'L' || it == 'l' }
                ) {
                    report(CodeSmell(issue, Entity.from(expression),
                        "Color($text) is a hardcoded literal. Map to a semantic token: appTheme.colors.*"))
                }
            }
            // Color(red, green, blue) or Color(red, green, blue, alpha) — float RGB
            in 3..4 -> {
                val first = args[0].getArgumentExpression()?.text ?: return
                if (first.matches(Regex("\\d+(\\.\\d+)?f?"))) {
                    report(CodeSmell(issue, Entity.from(expression),
                        "Color(r,g,b) is a hardcoded RGB literal. Map to a semantic token: appTheme.colors.*"))
                }
            }
        }
    }
}
