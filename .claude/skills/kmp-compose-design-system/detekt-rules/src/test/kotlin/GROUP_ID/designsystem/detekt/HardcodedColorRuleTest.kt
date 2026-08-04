package GROUP_ID.designsystem.detekt

import io.gitlab.arturbosch.detekt.test.lint
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HardcodedColorRuleTest {

    private fun rule() = HardcodedColorRule(io.gitlab.arturbosch.detekt.api.Config.empty)

    @Test fun `flags hex Color literal`() {
        val findings = rule().lint("""
            import androidx.compose.ui.graphics.Color
            val c = Color(0xFF1A73E8)
        """.trimIndent())
        assertEquals(1, findings.size)
    }

    @Test fun `flags float RGB Color`() {
        val findings = rule().lint("""
            import androidx.compose.ui.graphics.Color
            val c = Color(0.1f, 0.5f, 0.9f)
        """.trimIndent())
        assertEquals(1, findings.size)
    }

    @Test fun `does not flag Color in Colors_kt`() {
        val findings = rule().lint("""
            import androidx.compose.ui.graphics.Color
            val primary = Color(0xFF6200EE)
        """.trimIndent(), "AppColors.kt")
        assertTrue(findings.isEmpty())
    }

    @Test fun `does not flag semantic token usage`() {
        val findings = rule().lint("""
            val color = appTheme.colors.primary
        """.trimIndent())
        assertTrue(findings.isEmpty())
    }
}
