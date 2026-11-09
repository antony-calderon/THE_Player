// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // AGP 9 incluye Kotlin integrado, por eso no se declara kotlin.android aquí.
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
}
