plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.openfy"
    compileSdk = 36
    defaultConfig {
        applicationId = "io.github.artiomitprograming.openfy"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0-beta01"
    }

    signingConfigs {
        create("release") {
            val releaseKeystorePath = System.getenv("RELEASE_KEYSTORE_PATH")
            if (releaseKeystorePath != null && file(releaseKeystorePath).exists()) {
                storeFile = file(releaseKeystorePath)
                storePassword = System.getenv("RELEASE_KEYSTORE_PASSWORD") ?: ""
                keyAlias = System.getenv("RELEASE_KEY_ALIAS") ?: ""
                keyPassword = System.getenv("RELEASE_KEY_PASSWORD") ?: ""
            } else {
                initWith(getByName("debug"))
            }
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
      compose = true
      aidl = false
      buildConfig = false
      shaders = false
    }

    packaging {
      resources {
        excludes += "/META-INF/{AL2.0,LGPL2.1}"
      }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
  implementation(project(":core:audio"))
  implementation(project(":core:ui"))
  implementation(project(":features:themes"))
  implementation(project(":features:community"))
  implementation(project(":features:streamer"))

  val composeBom = platform(libs.androidx.compose.bom)
  implementation(composeBom)
  androidTestImplementation(composeBom)

  // Core Android dependencies
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)

  // Arch Components
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)

  // Compose
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material3.window.size)
  implementation(libs.androidx.compose.material.icons.extended)
  // Tooling
  debugImplementation(libs.androidx.compose.ui.tooling)
  // Instrumented tests
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  debugImplementation(libs.androidx.compose.ui.test.manifest)

  // Local tests: jUnit, coroutines, Android runner
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)

  // Instrumented tests: jUnit rules and runners
  androidTestImplementation(libs.androidx.test.core)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.espresso.core)

  // Navigation
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.lifecycle.viewmodel.navigation3)

  // Media3 ExoPlayer & MediaSession
  implementation(libs.media3.exoplayer)
  implementation(libs.media3.session)
  implementation(libs.media3.ui)
  implementation(libs.media3.common)

  // Icons Extended
  implementation(libs.androidx.compose.material.icons.extended)

  // Image loading & Dynamic Color Palette extraction
  implementation(libs.coil.compose)
  implementation(libs.androidx.palette)

  // Preferences & JSON
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.gson)

}
