package io.github.ronjunevaldoz.shadcncompose.catalog.docs

val componentDocsById: Map<String, ComponentDoc> =
    listOf(
        buttonDoc,
        cardDoc,
        badgeDoc,
        chipDoc,
        textFieldDoc,
        textDoc,
        labelDoc,
        checkboxDoc,
        radioGroupDoc,
        switchDoc,
        toggleDoc,
        sliderDoc,
        toggleGroupDoc,
        inputGroupDoc,
        buttonGroupDoc,
        textareaDoc,
    ).associateBy { it.id }
