plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.example.openfy.core.audio"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)

    // Media3 (ExoPlayer & MediaSession)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.session)
    implementation(libs.media3.common)
    implementation(libs.media3.ui)
    implementation(libs.media3.datasource)

    // Coroutines & Preferences & JSON
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.gson)

    testImplementation(libs.junit)
}
