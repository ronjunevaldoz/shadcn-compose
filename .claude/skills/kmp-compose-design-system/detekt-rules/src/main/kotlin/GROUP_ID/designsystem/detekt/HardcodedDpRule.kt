package GROUP_ID.designsystem.detekt

import io.gitlab.arturbosch.detekt.api.*
import org.jetbrains.kotlin.psi.*

/**
 * Flags raw N.dp literals inside layout modifiers and Spacer calls.
 * 0.dp and 1.dp are exempt (dividers). Token definition files are exempt.
 *
 * Token mapping: 4→xs  8→sm  12→md  16→lg  20→xl  24→xxl  32→xxxl
 */
class HardcodedDpRule(config: Config) : Rule(config) {

    override val issue = Issue(
        id = "HardcodedDp",
        severity = Severity.Warning,
        description = "Hardcoded .dp literal in a layout call. Use appTheme.spacing.* tokens instead.",
        debt = Debt.FIVE_MINS,
    )

    private val skipSuffixes = setOf("Spacing.kt", "Tokens.kt", "Theme.kt", "Styles.kt")

    // Modifier calls that should reference spacing tokens
    private val layoutCalls = setOf(
        "padding", "height", "width", "size", "offset", "requiredHeight",
        "requiredWidth", "requiredSize", "wrapContentHeight", "wrapContentWidth",
    )

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)

        val filePath = expression.containingKtFile.virtualFilePath
        if (skipSuffixes.any { filePath.endsWith(it) }) return
        if (filePath.contains("/theme/") || filePath.contains("/tokens/")) return

        val name = expression.calleeExpression?.text ?: return
        if (name !in layoutCalls && name != "Spacer") return

        expression.valueArguments.forEach { arg ->
            val argExpr = arg.getArgumentExpression() ?: return@forEach
            checkForDpLiteral(argExpr)
        }
    }

    private fun checkForDpLiteral(expr: KtExpression) {
        when (expr) {
            is KtDotQualifiedExpression -> {
                if (expr.selectorExpression?.text == "dp") {
                    val raw = expr.receiverExpression.text.trimEnd('f').toDoubleOrNull() ?: return
                    if (raw <= 1.0) return  // 0.dp and 1.dp are dividers — exempt
                    val suggestion = tokenFor(raw)
                    report(CodeSmell(issue, Entity.from(expr),
                        "${raw.toInt()}.dp is a hardcoded value. Use appTheme.spacing.$suggestion"))
                }
            }
            // Recurse into modifier chains: Modifier.padding(16.dp).height(8.dp)
            is KtCallExpression -> expr.valueArguments.forEach { a ->
                a.getArgumentExpression()?.let { checkForDpLiteral(it) }
            }
        }
    }

    private fun tokenFor(dp: Double) = when (dp.toInt()) {
        4    -> "xs"
        8    -> "sm"
        12   -> "md"
        16   -> "lg"
        20   -> "xl"
        24   -> "xxl"
        32   -> "xxxl"
        else -> "<closest token>"
    }
}
