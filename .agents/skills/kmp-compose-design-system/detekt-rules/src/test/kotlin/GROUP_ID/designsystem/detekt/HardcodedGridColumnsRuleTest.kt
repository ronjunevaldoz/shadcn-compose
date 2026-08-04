package GROUP_ID.designsystem.detekt

import io.gitlab.arturbosch.detekt.test.lint
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HardcodedGridColumnsRuleTest {

    private fun rule() = HardcodedGridColumnsRule(io.gitlab.arturbosch.detekt.api.Config.empty)

    @Test fun `flags GridCells Fixed 2`() {
        val findings = rule().lint("""
            val grid = LazyVerticalGrid(columns = GridCells.Fixed(2))
        """.trimIndent())
        assertEquals(1, findings.size)
        assertTrue(findings[0].message.contains("Adaptive"))
    }

    @Test fun `flags GridCells Fixed 3`() {
        val findings = rule().lint("""
            val grid = LazyVerticalGrid(columns = GridCells.Fixed(3))
        """.trimIndent())
        assertEquals(1, findings.size)
    }

    @Test fun `does not flag GridCells Fixed 1`() {
        val findings = rule().lint("""
            val grid = LazyVerticalGrid(columns = GridCells.Fixed(1))
        """.trimIndent())
        assertTrue(findings.isEmpty())
    }

    @Test fun `does not flag GridCells Adaptive`() {
        val findings = rule().lint("""
            val grid = LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 160.dp))
        """.trimIndent())
        assertTrue(findings.isEmpty())
    }
}
