import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.vanniktechPublish)
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
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
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
