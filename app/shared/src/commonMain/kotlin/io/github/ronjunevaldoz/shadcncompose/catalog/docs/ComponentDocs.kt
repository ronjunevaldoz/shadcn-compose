package io.github.ronjunevaldoz.shadcncompose.catalog.docs

val componentDocsById: Map<String, ComponentDoc> =
    listOf(
        buttonDoc,
        cardDoc,
        badgeDoc,
        chipDoc,
        textFieldDoc,
        textDoc,
    ).associateBy { it.id }
