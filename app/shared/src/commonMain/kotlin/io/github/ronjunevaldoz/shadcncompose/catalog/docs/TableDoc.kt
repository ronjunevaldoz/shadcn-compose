package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.ui.Modifier
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTable
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTableCell
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTableHeadCell
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTableHeaderRow
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTableRow

private data class InvoiceRow(val id: String, val status: String, val amount: String)

private val sampleInvoices =
    listOf(
        InvoiceRow("INV001", "Paid", "$250.00"),
        InvoiceRow("INV002", "Pending", "$150.00"),
        InvoiceRow("INV003", "Unpaid", "$350.00"),
    )

val tableDoc =
    ComponentDoc(
        id = "table",
        title = "Table",
        description = "A responsive data table with a bordered header and hairline row separators.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTable

            ShadcnTable {
                ShadcnTableHeaderRow {
                    ShadcnTableHeadCell("Invoice", Modifier.weight(1f))
                    ShadcnTableHeadCell("Status")
                }
                ShadcnTableRow(isLast = true) {
                    ShadcnTableCell("INV001", Modifier.weight(1f))
                    ShadcnTableCell("Paid")
                }
            }
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        ShadcnTable {
                            ShadcnTableHeaderRow {
                                ShadcnTableHeadCell("Invoice", Modifier.weight(1f))
                                ShadcnTableHeadCell("Status", Modifier.weight(1f))
                                ShadcnTableHeadCell("Amount")
                            }
                            sampleInvoices.forEachIndexed { index, invoice ->
                                ShadcnTableRow(isLast = index == sampleInvoices.lastIndex) {
                                    ShadcnTableCell(invoice.id, Modifier.weight(1f))
                                    ShadcnTableCell(invoice.status, Modifier.weight(1f), muted = true)
                                    ShadcnTableCell(invoice.amount)
                                }
                            }
                        }
                        """.trimIndent(),
                    preview = {
                        ShadcnTable {
                            ShadcnTableHeaderRow {
                                ShadcnTableHeadCell("Invoice", Modifier.weight(1f))
                                ShadcnTableHeadCell("Status", Modifier.weight(1f))
                                ShadcnTableHeadCell("Amount")
                            }
                            sampleInvoices.forEachIndexed { index, invoice ->
                                ShadcnTableRow(isLast = index == sampleInvoices.lastIndex) {
                                    ShadcnTableCell(invoice.id, Modifier.weight(1f))
                                    ShadcnTableCell(invoice.status, Modifier.weight(1f), muted = true)
                                    ShadcnTableCell(invoice.amount)
                                }
                            }
                        }
                    },
                ),
            ),
    )
