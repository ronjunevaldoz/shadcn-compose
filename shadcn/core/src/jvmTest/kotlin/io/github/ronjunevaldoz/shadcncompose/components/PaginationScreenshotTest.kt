package io.github.ronjunevaldoz.shadcncompose.components

import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

class PaginationScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("pagination_states", darkTheme = darkTheme) {
            ShadcnPagination(
                items =
                    listOf(
                        ShadcnPaginationItem.Page(1),
                        ShadcnPaginationItem.Page(2),
                        ShadcnPaginationItem.Page(3),
                        ShadcnPaginationItem.Ellipsis,
                        ShadcnPaginationItem.Page(10),
                    ),
                currentPage = 2,
                onPageChange = {},
            )
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)
}
