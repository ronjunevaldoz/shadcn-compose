package GROUP_ID.designsystem.detekt

import io.gitlab.arturbosch.detekt.test.lint
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ComponentRegistryRuleTest {

    private fun rule() = ComponentRegistryRule(io.gitlab.arturbosch.detekt.api.Config.empty)

    @Test fun `flags reimplemented Button outside design system`() {
        val findings = rule().lint("""
            import androidx.compose.runtime.Composable
            @Composable
            fun MyButton(label: String) {}
        """.trimIndent(), "feature/auth/ui/AuthButton.kt")
        assertEquals(1, findings.size)
        assertTrue(findings[0].message.contains("AppButton"))
    }

    @Test fun `flags reimplemented Card outside design system`() {
        val findings = rule().lint("""
            import androidx.compose.runtime.Composable
            @Composable
            fun ProductCard(title: String) {}
        """.trimIndent(), "feature/home/ui/ProductCard.kt")
        assertEquals(1, findings.size)
    }

    @Test fun `does not flag AppButton in design system`() {
        val findings = rule().lint("""
            import androidx.compose.runtime.Composable
            @Composable
            fun AppButton(label: String) {}
        """.trimIndent(), "core/designsystem/components/AppButton.kt")
        assertTrue(findings.isEmpty())
    }

    @Test fun `does not flag Preview composable`() {
        val findings = rule().lint("""
            import androidx.compose.runtime.Composable
            @Composable
            fun AppButtonPreview() {}
        """.trimIndent(), "core/designsystem/previews/AppButtonPreview.kt")
        assertTrue(findings.isEmpty())
    }
}
