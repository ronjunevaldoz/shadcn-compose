package GROUP_ID.designsystem.detekt

import io.gitlab.arturbosch.detekt.test.lint
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedundantScreenTitleRuleTest {

    private fun rule() = RedundantScreenTitleRule(io.gitlab.arturbosch.detekt.api.Config.empty)

    @Test fun `flags Text with string literal in Content composable`() {
        val findings = rule().lint("""
            import androidx.compose.runtime.Composable
            @Composable
            fun CommunityContent(state: State) {
                Column {
                    Text("Community")
                }
            }
        """.trimIndent())
        assertEquals(1, findings.size)
        assertTrue(findings[0].message.contains("AppTopAppBar"))
    }

    @Test fun `flags AppText with string literal in Screen composable`() {
        val findings = rule().lint("""
            import androidx.compose.runtime.Composable
            @Composable
            fun ProfileScreen() {
                AppText("Profile")
            }
        """.trimIndent())
        assertEquals(1, findings.size)
    }

    @Test fun `does not flag dynamic text (state variable)`() {
        val findings = rule().lint("""
            import androidx.compose.runtime.Composable
            @Composable
            fun CommunityContent(state: State) {
                Text(state.title)
            }
        """.trimIndent())
        assertTrue(findings.isEmpty())
    }

    @Test fun `does not flag Text in Preview composable`() {
        val findings = rule().lint("""
            import androidx.compose.runtime.Composable
            @Composable
            fun CommunityContentPreview() {
                Text("Community")
            }
        """.trimIndent())
        assertTrue(findings.isEmpty())
    }

    @Test fun `does not flag Text in design system module`() {
        val findings = rule().lint("""
            import androidx.compose.runtime.Composable
            @Composable
            fun AppButtonContent() {
                Text("label")
            }
        """.trimIndent(), "core/designsystem/components/AppButton.kt")
        assertTrue(findings.isEmpty())
    }

    @Test fun `does not flag helper composable not ending in Content or Screen`() {
        val findings = rule().lint("""
            import androidx.compose.runtime.Composable
            @Composable
            fun GuildCard() {
                Text("LordNine PH")
            }
        """.trimIndent())
        assertTrue(findings.isEmpty())
    }
}
