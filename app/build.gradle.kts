plugins {
    // AGP 9 trae soporte de Kotlin integrado: NO se aplica kotlin.android
    // (hacerlo duplica la extensión 'kotlin' y rompe el build).
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

android {
    namespace = "com.antuan.the_player"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.antuan.the_player"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

// El bloque kotlin { } debe ser top-level (hermano de android { }), no anidado.
// En AGP 9 anidarlo provoca "Suspicious receiver type ... ApplicationExtension".
//
// Fijamos un toolchain de Java 17 para desacoplar la compilación del JBR 25
// de Android Studio y hacer el build reproducible entre máquinas.
kotlin {
    jvmToolchain(17)
}

// Exporta los esquemas versionados de Room (útiles para migraciones y tests).
// Elimina el warning de "Schema export directory was not provided".
room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    // Base del proyecto (ya existentes)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)

    // Lifecycle / ViewModel
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Compose (gestionado por el BOM)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    debugImplementation(libs.androidx.ui.tooling)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Hilt (inyección de dependencias)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Media3 / ExoPlayer
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.common)
    implementation(libs.androidx.media3.ui)

    // Coil (carátulas de álbum)
    implementation(libs.coil.compose)

    // DataStore (preferencias de usuario / ajustes)
    implementation(libs.androidx.datastore.preferences)

    // Palette (extracción de color de carátulas para el efecto glass)
    implementation(libs.androidx.palette.ktx)

    // Glance (App Widget de pantalla de inicio)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    // Room (persistencia de playlists y la cola fusionada)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
