package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import io.github.ronjunevaldoz.shadcncompose.components.ShadcnBreadcrumb
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnBreadcrumbEllipsis
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnBreadcrumbLink
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnBreadcrumbPage
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnBreadcrumbSeparator

val breadcrumbDoc =
    ComponentDoc(
        id = "breadcrumb",
        title = "Breadcrumb",
        description = "A row of navigation links showing the current page's location in a hierarchy.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.*

            ShadcnBreadcrumb {
                ShadcnBreadcrumbLink("Home", onClick = {})
                ShadcnBreadcrumbSeparator()
                ShadcnBreadcrumbPage("Settings")
            }
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        ShadcnBreadcrumb {
                            ShadcnBreadcrumbLink("Home", onClick = {})
                            ShadcnBreadcrumbSeparator()
                            ShadcnBreadcrumbLink("Components", onClick = {})
                            ShadcnBreadcrumbSeparator()
                            ShadcnBreadcrumbPage("Breadcrumb")
                        }
                        """.trimIndent(),
                    preview = {
                        ShadcnBreadcrumb {
                            ShadcnBreadcrumbLink("Home", onClick = {})
                            ShadcnBreadcrumbSeparator()
                            ShadcnBreadcrumbLink("Components", onClick = {})
                            ShadcnBreadcrumbSeparator()
                            ShadcnBreadcrumbPage("Breadcrumb")
                        }
                    },
                ),
                ComponentExample(
                    title = "With ellipsis",
                    code =
                        """
                        ShadcnBreadcrumb {
                            ShadcnBreadcrumbLink("Home", onClick = {})
                            ShadcnBreadcrumbSeparator()
                            ShadcnBreadcrumbEllipsis()
                            ShadcnBreadcrumbSeparator()
                            ShadcnBreadcrumbPage("Settings")
                        }
                        """.trimIndent(),
                    preview = {
                        ShadcnBreadcrumb {
                            ShadcnBreadcrumbLink("Home", onClick = {})
                            ShadcnBreadcrumbSeparator()
                            ShadcnBreadcrumbEllipsis()
                            ShadcnBreadcrumbSeparator()
                            ShadcnBreadcrumbPage("Settings")
                        }
                    },
                ),
            ),
    )
