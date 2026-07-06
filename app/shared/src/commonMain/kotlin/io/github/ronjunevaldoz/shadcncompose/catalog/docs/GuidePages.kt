package io.github.ronjunevaldoz.shadcncompose.catalog.docs

val guidePagesById: Map<String, GuidePage> =
    listOf(
        introductionPage,
        installationPage,
        themingPage,
        darkModePage,
        typographyPage,
    ).associateBy { it.id }
