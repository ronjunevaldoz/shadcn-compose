package GROUP_ID.designsystem.detekt

import io.gitlab.arturbosch.detekt.api.*
import org.jetbrains.kotlin.psi.*

/**
 * Flags @Composable functions in feature modules whose names end with a known
 * design system component suffix — these are likely reimplementations of a design
 * system component rather than extensions of it.
 *
 * Configure `componentPrefix` in detekt-design-system.yml to match your project's
 * prefix (default: "App").
 *
 * NOT flagged:
 *   - Functions whose name starts with the componentPrefix (e.g. AppButton)
 *   - Functions in core/designsystem/ or files ending in Preview.kt
 *   - Functions with fewer than 1 parameter (likely non-interactive wrappers)
 *
 * FLAGGED:
 *   - `fun MyButton(...)` in feature/*/ui/  — ends in "Button" outside DS module
 *   - `fun CustomCard(...)` in feature/*/   — ends in "Card" outside DS module
 */
class ComponentRegistryRule(config: Config) : Rule(config) {

    override val issue = Issue(
        id = "ComponentRegistryViolation",
        severity = Severity.Warning,
        description = "Custom composable name matches a design system component. Prefer using the design system component or extending it via the variant system.",
        debt = Debt.TWENTY_MINS,
    )

    private val componentPrefix: String by config.valueOrDefault("componentPrefix", "App")

    private val dsComponentSuffixes = setOf(
        "Button", "Card", "Text", "Badge", "Chip", "TextField",
        "Dialog", "Sheet", "Toast", "TopAppBar", "NavigationBar",
        "Checkbox", "RadioButton", "Switch", "Slider", "Tabs",
        "Icon", "IconButton", "Spinner", "Skeleton", "Progress",
        "Avatar", "Tooltip", "Popover", "Accordion", "Label", "Separator",
    )

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)

        val isComposable = function.annotationEntries
            .any { it.shortName?.asString() == "Composable" }
        if (!isComposable) return

        val filePath = function.containingKtFile.virtualFilePath
        if (filePath.contains("/designsystem/")) return
        if (filePath.endsWith("Preview.kt")) return

        val name = function.name ?: return
        if (name.startsWith(componentPrefix)) return  // proper DS component or Preview

        val matched = dsComponentSuffixes.firstOrNull { name.endsWith(it) } ?: return

        report(CodeSmell(issue, Entity.from(function),
            "'$name' reimplements a ${componentPrefix}$matched-shaped component outside the design system. " +
            "Use '${componentPrefix}$matched' directly, or add a new variant to the design system " +
            "rather than creating a parallel component."))
    }
}
