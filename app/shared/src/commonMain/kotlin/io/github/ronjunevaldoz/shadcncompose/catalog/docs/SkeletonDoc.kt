package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnSkeleton

val skeletonDoc =
    ComponentDoc(
        id = "skeleton",
        title = "Skeleton",
        description = "A pulsing placeholder block shown while content is loading.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnSkeleton

            ShadcnSkeleton(modifier = Modifier.size(width = 200.dp, height = 20.dp))
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ShadcnSkeleton(modifier = Modifier.size(width = 250.dp, height = 20.dp))
                            ShadcnSkeleton(modifier = Modifier.size(width = 200.dp, height = 20.dp))
                        }
                        """.trimIndent(),
                    preview = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ShadcnSkeleton(modifier = Modifier.size(width = 250.dp, height = 20.dp))
                            ShadcnSkeleton(modifier = Modifier.size(width = 200.dp, height = 20.dp))
                        }
                    },
                ),
                ComponentExample(
                    title = "Avatar placeholder",
                    code = "ShadcnSkeleton(modifier = Modifier.size(40.dp).clip(CircleShape))",
                    preview = {
                        ShadcnSkeleton(modifier = Modifier.size(40.dp))
                    },
                ),
            ),
    )
