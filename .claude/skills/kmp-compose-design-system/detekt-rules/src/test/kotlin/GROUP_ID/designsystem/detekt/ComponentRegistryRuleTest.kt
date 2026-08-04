package GROUP_ID.designsystem.detekt

import io.gitlab.arturbosch.detekt.test.lint
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ComponentRegistryRuleTest {

    private fun rule() = ComponentRegistryRule(io.gitlab.arturbosch.detekt.api.Config.empty)

    @Test fun `flags reimplemented Button built from raw primitives`() {
        val findings = rule().lint("""
            import androidx.compose.runtime.Composable
            @Composable
            fun MyButton(label: String) {
                Button(onClick = {}) { Text(label) }
            }
        """.trimIndent(), "feature/auth/ui/AuthButton.kt")
        assertEquals(1, findings.size)
        assertTrue(findings[0].message.contains("AppButton"))
    }

    @Test fun `flags reimplemented Card built from raw primitives`() {
        val findings = rule().lint("""
            import androidx.compose.runtime.Composable
            @Composable
            fun ProductCard(title: String) {
                Card { Text(title) }
            }
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

    @Test fun `does not flag a wrapper that composes the design system component`() {
        val findings = rule().lint("""
            import androidx.compose.runtime.Composable
            @Composable
            fun ProductCard(title: String) {
                AppCard { AppText(title) }
            }
        """.trimIndent(), "feature/home/ui/ProductCard.kt")
        assertTrue(findings.isEmpty())
    }

    @Test fun `does not flag a zero-parameter composable`() {
        val findings = rule().lint("""
            import androidx.compose.runtime.Composable
            @Composable
            fun WelcomeCard() {
                Card { Text("Welcome") }
            }
        """.trimIndent(), "feature/home/ui/WelcomeCard.kt")
        assertTrue(findings.isEmpty())
    }
}
