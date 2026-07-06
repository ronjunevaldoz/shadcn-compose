package GROUP_ID.designsystem.detekt

import io.gitlab.arturbosch.detekt.api.*
import org.jetbrains.kotlin.psi.*

/**
 * Enforces that feature UI modules do not import design system token classes directly.
 *
 * Tokens are accessed at runtime via the `appTheme` @Composable accessor.
 * Importing AppColors, AppSpacing, etc. directly bypasses the CompositionLocal
 * and may return un-themed default values (the wrong light-mode values in dark mode).
 *
 * Allowed — these import the component API, not the raw tokens:
 *   import GROUP_ID.core.designsystem.components.AppButton
 *   import GROUP_ID.core.designsystem.theme.AppTheme
 *
 * Flagged — these import the token data classes directly:
 *   import GROUP_ID.core.designsystem.tokens.AppColors
 *   import GROUP_ID.core.designsystem.tokens.AppSpacing
 *   import GROUP_ID.core.designsystem.typography.AppTypography
 *
 * Only applied to files under feature/*/ui/ to avoid false positives in test
 * helpers and design system documentation.
 */
class ImportBoundaryRule(config: Config) : Rule(config) {

    override val issue = Issue(
        id = "DesignTokenImportBoundary",
        severity = Severity.Error,
        description = "Direct token import in a feature UI module. Access tokens via appTheme.* inside @Composable functions, not via direct class imports.",
        debt = Debt.TEN_MINS,
    )

    // Package fragments that indicate a raw token import
    private val tokenFragments = listOf(
        ".tokens.", ".spacing.", ".colors.", ".typography.", ".shapes.",
    )

    override fun visitImportDirective(importDirective: KtImportDirective) {
        super.visitImportDirective(importDirective)

        val filePath = importDirective.containingKtFile.virtualFilePath
        // Scope: only feature UI source sets
        if (!filePath.contains("/feature/")) return
        if (!filePath.contains("/ui/")) return
        // Skip tests
        if (filePath.contains("Test") || filePath.contains("/test/")) return

        val importPath = importDirective.importedFqName?.asString() ?: return
        if (!importPath.contains("designsystem")) return

        val flagged = tokenFragments.any { fragment -> importPath.contains(fragment) }
        if (!flagged) return

        val symbol = importPath.substringAfterLast(".")
        report(CodeSmell(issue, Entity.from(importDirective),
            "Direct token import '$symbol' in a feature UI module. " +
            "Remove this import and access the token via: val t = appTheme; t.${inferAccessor(importPath)}.*"))
    }

    private fun inferAccessor(importPath: String) = when {
        importPath.contains(".colors.")     -> "colors"
        importPath.contains(".spacing.")    -> "spacing"
        importPath.contains(".typography.") -> "typography"
        importPath.contains(".shapes.")     -> "shapes"
        else -> "<token-property>"
    }
}
