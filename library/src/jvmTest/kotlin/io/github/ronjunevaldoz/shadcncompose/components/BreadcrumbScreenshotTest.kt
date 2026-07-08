package io.github.ronjunevaldoz.shadcncompose.components

import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

class BreadcrumbScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("breadcrumb_states", darkTheme = darkTheme) {
            ShadcnBreadcrumb {
                ShadcnBreadcrumbLink("Home", onClick = {})
                ShadcnBreadcrumbSeparator()
                ShadcnBreadcrumbLink("Components", onClick = {})
                ShadcnBreadcrumbSeparator()
                ShadcnBreadcrumbPage("Breadcrumb")
            }
        }
    }

    private fun withEllipsis(darkTheme: Boolean) {
        snapshot("breadcrumb_ellipsis", darkTheme = darkTheme) {
            ShadcnBreadcrumb {
                ShadcnBreadcrumbLink("Home", onClick = {})
                ShadcnBreadcrumbSeparator()
                ShadcnBreadcrumbEllipsis()
                ShadcnBreadcrumbSeparator()
                ShadcnBreadcrumbPage("Settings")
            }
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)

    @Test fun with_ellipsis_light() = withEllipsis(darkTheme = false)
}
