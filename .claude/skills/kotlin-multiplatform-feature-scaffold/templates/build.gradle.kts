// Top-level build file.
// Convention plugins handle all module-level configuration.
// Add plugin declarations here only to make them available to subprojects.
plugins {
    alias(libs.plugins.androidApplication)     apply false
    alias(libs.plugins.androidKmpLibrary)      apply false
    alias(libs.plugins.kotlinMultiplatform)    apply false
    alias(libs.plugins.kotlinAndroid)          apply false
    alias(libs.plugins.kotlinSerialization)    apply false
    alias(libs.plugins.composeMultiplatform)   apply false
    alias(libs.plugins.composeCompiler)        apply false
    alias(libs.plugins.ksp)                    apply false
    alias(libs.plugins.koin)                   apply false
    alias(libs.plugins.sqldelight)             apply false
    alias(libs.plugins.buildkonfig)            apply false
}
