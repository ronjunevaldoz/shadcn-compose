plugins {
    `kotlin-dsl`
}

dependencies {
    // Plugin classpaths for use inside convention plugins.
    // Versions come from the shared libs.versions.toml via the build-logic settings.
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.sqldelight.gradlePlugin)
    compileOnly(libs.buildkonfig.gradlePlugin)
}
