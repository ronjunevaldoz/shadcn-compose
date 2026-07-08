package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCalendar
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCalendarDate

val calendarDoc =
    ComponentDoc(
        id = "calendar",
        title = "Calendar",
        description = "A single-month date picker grid with a highlighted selection and today marker.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCalendar

            var year by remember { mutableStateOf(2026) }
            var month by remember { mutableStateOf(3) }
            var selected by remember { mutableStateOf<ShadcnCalendarDate?>(null) }
            ShadcnCalendar(
                year = year, month = month,
                onMonthChange = { y, m -> year = y; month = m },
                selected = selected, onSelectedChange = { selected = it },
                today = ShadcnCalendarDate(2026, 3, 15),
            )
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        var year by remember { mutableStateOf(2026) }
                        var month by remember { mutableStateOf(3) }
                        var selected by remember { mutableStateOf<ShadcnCalendarDate?>(ShadcnCalendarDate(2026, 3, 10)) }
                        ShadcnCalendar(
                            year = year, month = month,
                            onMonthChange = { y, m -> year = y; month = m },
                            selected = selected, onSelectedChange = { selected = it },
                            today = ShadcnCalendarDate(2026, 3, 15),
                        )
                        """.trimIndent(),
                    preview = {
                        var year by remember { mutableStateOf(2026) }
                        var month by remember { mutableStateOf(3) }
                        var selected by
                            remember { mutableStateOf<ShadcnCalendarDate?>(ShadcnCalendarDate(2026, 3, 10)) }
                        ShadcnCalendar(
                            year = year,
                            month = month,
                            onMonthChange = { y, m ->
                                year = y
                                month = m
                            },
                            selected = selected,
                            onSelectedChange = { selected = it },
                            today = ShadcnCalendarDate(2026, 3, 15),
                        )
                    },
                ),
            ),
    )
