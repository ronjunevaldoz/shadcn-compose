package GROUP_ID.designsystem.detekt

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

class DesignSystemRuleSetProvider : RuleSetProvider {
    override val ruleSetId = "design-system"

    override fun instance(config: Config) = RuleSet(
        ruleSetId,
        listOf(
            HardcodedColorRule(config),
            HardcodedDpRule(config),
            MaterialThemeUsageRule(config),
            DirectTextStyleRule(config),
            NestedContainerRule(config),
            ComponentRegistryRule(config),
            ImportBoundaryRule(config),
            RedundantScreenTitleRule(config),
            HardcodedGridColumnsRule(config),
        )
    )
}
