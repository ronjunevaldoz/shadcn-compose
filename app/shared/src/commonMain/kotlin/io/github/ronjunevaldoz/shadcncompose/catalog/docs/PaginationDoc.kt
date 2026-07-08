package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnPagination
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnPaginationItem

val paginationDoc =
    ComponentDoc(
        id = "pagination",
        title = "Pagination",
        description = "Page-number navigation with Previous/Next controls and ellipsis gaps.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnPagination
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnPaginationItem

            var page by remember { mutableStateOf(1) }
            ShadcnPagination(
                items = listOf(
                    ShadcnPaginationItem.Page(1),
                    ShadcnPaginationItem.Page(2),
                    ShadcnPaginationItem.Ellipsis,
                    ShadcnPaginationItem.Page(10),
                ),
                currentPage = page,
                onPageChange = { page = it },
            )
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        var page by remember { mutableStateOf(1) }
                        ShadcnPagination(
                            items = listOf(
                                ShadcnPaginationItem.Page(1),
                                ShadcnPaginationItem.Page(2),
                                ShadcnPaginationItem.Page(3),
                                ShadcnPaginationItem.Ellipsis,
                                ShadcnPaginationItem.Page(10),
                            ),
                            currentPage = page,
                            onPageChange = { page = it },
                        )
                        """.trimIndent(),
                    preview = {
                        var page by remember { mutableStateOf(1) }
                        ShadcnPagination(
                            items =
                                listOf(
                                    ShadcnPaginationItem.Page(1),
                                    ShadcnPaginationItem.Page(2),
                                    ShadcnPaginationItem.Page(3),
                                    ShadcnPaginationItem.Ellipsis,
                                    ShadcnPaginationItem.Page(10),
                                ),
                            currentPage = page,
                            onPageChange = { page = it },
                        )
                    },
                ),
            ),
    )
