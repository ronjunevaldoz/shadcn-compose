import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.vanniktechPublish)
    alias(libs.plugins.roborazzi)
}

roborazzi {
    outputDir = project.file("src/jvmTest/snapshots")
}

group = project.property("GROUP") as String
version = project.property("VERSION_NAME") as String

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ShadcnCompose"
            isStatic = true
        }
    }

    jvm()

    js {
        browser()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    androidLibrary {
        namespace = "io.github.ronjunevaldoz.shadcncompose"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
        }
        commonMain.dependencies {
            // No Material dependency: this library is fully custom, built on
            // the Compose Styles API (@ExperimentalStylesApi) instead.
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            // Real Maven Central release (verified against repo1.maven.org's own
            // maven-metadata.xml, not the search index, which lags actual publishes) --
            // see .claude/AGENTS.md "Planned dependencies". `implementation`, not `api`,
            // since we're not exposing any tailwind-compose type in our own public API
            // surface -- component internals may call its Modifier utilities, but no
            // Shadcn* function signature should ever require a caller to import
            // `io.github.ronjunevaldoz.tailwind.*`.
            implementation(libs.tailwind.compose)
            // tailwind-style: Ring/Border/Shadow/Color/Opacity/Sizing/Transform/Transition/
            // Typography/ZIndex ported onto the real Compose Styles API -- same Compose
            // Multiplatform version this module already targets (1.11.1), so no version
            // isolation needed (unlike tailwind-style-experimental, which is pinned to
            // 1.12.0-beta01 and can't be depended on here). Not re-exported via
            // tailwind-compose's facade yet, so pulled in directly.
            implementation(libs.tailwind.style)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmTest.dependencies {
            // Robolectric-less JVM/Desktop screenshot testing: `roborazzi` and
            // `roborazzi-junit-rule` only publish as Android .aar (Robolectric-based),
            // which doesn't resolve for this module's plain jvm() target -- only
            // `roborazzi-compose-desktop` publishes a real desktop/jvm artifact.
            implementation(compose.desktop.currentOs)
            implementation(compose.desktop.uiTestJUnit4)
            implementation(libs.roborazzi.compose.desktop)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}

mavenPublishing {
    // Central Portal publish target, with automatic release: once uploaded and
    // validated, the deployment publishes on its own with no manual "release" click
    // in the Central Portal UI. Needs real Sonatype Central Portal credentials --
    // ORG_GRADLE_PROJECT_mavenCentralUsername/mavenCentralPassword (env vars or
    // ~/.gradle/gradle.properties, never committed here) -- `publish` fails without
    // them. `publishToMavenLocal` works with no credentials and is the way to verify
    // this wiring locally.
    publishToMavenCentral(automaticRelease = true)

    // Maven Central rejects unsigned artifacts. Resolves a signing key the same way
    // tailwind-compose/heroicons-compose do -- either the standard `signing.keyId` /
    // `signing.password` / `signing.secretKeyRingFile` Gradle properties (keyring
    // file) or the in-memory equivalents (ORG_GRADLE_PROJECT_signingInMemoryKey /
    // signingInMemoryKeyPassword / signingInMemoryKeyId, preferred for CI since no
    // keyring file needs to exist on disk). No key material lives in this repo.
    signAllPublications()

    coordinates(artifactId = "shadcn-compose")

    pom {
        name = "shadcn-compose"
        description = "A shadcn/ui-inspired component library for Compose Multiplatform -- " +
            "token-based theming and sealed variant systems built on the Compose Styles API, " +
            "no Material dependency."
        url = "https://github.com/ronjunevaldoz/shadcn-compose"
        inceptionYear = "2026"

        licenses {
            license {
                name = "Apache-2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0"
            }
        }

        developers {
            developer {
                id = "ronjunevaldoz"
                name = "Ron June Valdoz"
                url = "https://github.com/ronjunevaldoz"
            }
        }

        scm {
            url = "https://github.com/ronjunevaldoz/shadcn-compose"
            connection = "scm:git:git://github.com/ronjunevaldoz/shadcn-compose.git"
            developerConnection = "scm:git:ssh://git@github.com/ronjunevaldoz/shadcn-compose.git"
        }
    }
}
