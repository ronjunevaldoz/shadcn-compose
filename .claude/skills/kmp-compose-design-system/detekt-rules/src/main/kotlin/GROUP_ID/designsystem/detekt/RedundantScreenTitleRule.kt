package GROUP_ID.designsystem.detekt

import io.gitlab.arturbosch.detekt.api.*
import org.jetbrains.kotlin.psi.*

/**
 * Flags Text() or AppText() calls inside *Content or *Screen composables where
 * the argument is a plain string literal — these are duplicate screen titles that
 * belong in AppTopAppBar(title = "…") instead.
 *
 * The pattern this catches:
 *
 *   @Composable
 *   fun CommunityContent(…) {
 *       Column {
 *           AppText("Community")   ← flagged: title belongs in AppTopAppBar
 *           …
 *       }
 *   }
 *
 * NOT flagged:
 *   - Text inside a Preview composable (*Preview suffix)
 *   - Text with a non-string-literal argument (e.g. dynamic state.title)
 *   - Text inside the design system itself (core/designsystem/)
 *   - Text composables that are not a direct or near-top descendant of the
 *     outermost layout call (e.g. text deep inside an item composable)
 *
 * Severity is Warning (not Error) because legitimate uses exist — e.g. a
 * section header inside content. Review before removing.
 */
class RedundantScreenTitleRule(config: Config) : Rule(config) {

    override val issue = Issue(
        id = "RedundantScreenTitle",
        severity = Severity.Warning,
        description = "Text/AppText with a string literal inside a *Content/*Screen composable. " +
            "Screen titles belong in AppTopAppBar(title = …), not the content body.",
        debt = Debt.FIVE_MINS,
    )

    private val textFunctions = setOf("Text", "AppText")
    private val screenSuffixes = setOf("Content", "Screen")

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)

        val isComposable = function.annotationEntries
            .any { it.shortName?.asString() == "Composable" }
        if (!isComposable) return

        val name = function.name ?: return
        if (screenSuffixes.none { name.endsWith(it) }) return
        if (name.endsWith("Preview")) return

        val filePath = function.containingKtFile.virtualFilePath
        if (filePath.contains("/designsystem/")) return

        // Walk the function body looking for top-level Text/AppText with string literals
        function.bodyBlockExpression?.accept(object : KtTreeVisitorVoid() {
            override fun visitCallExpression(expression: KtCallExpression) {
                val callee = expression.calleeExpression?.text ?: return
                if (callee !in textFunctions) {
                    super.visitCallExpression(expression)
                    return
                }

                // Check if the first arg is a plain string literal
                val firstArg = expression.valueArguments.firstOrNull()
                    ?.getArgumentExpression() ?: return
                if (firstArg is KtStringTemplateExpression && !firstArg.hasInterpolation()) {
                    report(CodeSmell(issue, Entity.from(expression),
                        "$callee(\"${firstArg.text.trim('"')}\") in '$name' looks like a screen title. " +
                        "Move it to AppTopAppBar(title = …) and remove it from the content body."))
                }
                super.visitCallExpression(expression)
            }
        })
    }
}
