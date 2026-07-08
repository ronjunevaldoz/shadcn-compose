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
    // Registers Central Portal as the publish target; this is dry-run wiring only --
    // no Sonatype credentials exist yet, so `publish` will fail until they're added
    // (via ORG_GRADLE_PROJECT_mavenCentralUsername/Password). `publishToMavenLocal`
    // works today and is the intended way to verify this configuration for now.
    publishToMavenCentral()

    // Signing is left disabled until a real GPG key is available -- signAllPublications()
    // would make even publishToMavenLocal fail with no key configured. Enable this
    // before the first real Maven Central release:
    // signAllPublications()

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
                name = "MIT"
                url = "https://opensource.org/licenses/MIT"
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
