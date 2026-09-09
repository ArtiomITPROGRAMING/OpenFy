# ==============================================================================
# OpenFy R8 Proguard Rules (FOSS & Offline-First Beta Release)
# ==============================================================================

# ------------------------------------------------------------------------------
# 1. KotlinX Serialization & Data Models
# ------------------------------------------------------------------------------
-keepattributes *Annotation*, InnerClasses, Signature
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep class **.openfy.**.model.** { *; }
-keepclassmembers class **.openfy.**.model.** { *; }
-keep class io.github.artiomitprograming.openfy.**.model.** { *; }
-keepclassmembers class io.github.artiomitprograming.openfy.**.model.** { *; }
-keep class com.example.openfy.**.model.** { *; }
-keepclassmembers class com.example.openfy.**.model.** { *; }
-keep class kotlinx.serialization.** { *; }

# ------------------------------------------------------------------------------
# 2. Native P2P Offline LocalShareServer & Sync Payloads
# ------------------------------------------------------------------------------
-keep class **.openfy.features.community.sync.** { *; }
-keepclassmembers class **.openfy.features.community.sync.** { *; }
-keep class io.github.artiomitprograming.openfy.features.community.sync.** { *; }
-keepclassmembers class io.github.artiomitprograming.openfy.features.community.sync.** { *; }
-keep class io.github.artiomitprograming.openfy.features.community.sync.LocalShareServer { *; }
-keepclassmembers class io.github.artiomitprograming.openfy.features.community.sync.LocalShareServer { *; }
-keep class io.github.artiomitprograming.openfy.features.community.sync.SharePayload { *; }
-keepclassmembers class io.github.artiomitprograming.openfy.features.community.sync.SharePayload { *; }
-keep class com.example.openfy.features.community.sync.** { *; }
-keepclassmembers class com.example.openfy.features.community.sync.** { *; }
-keep class com.example.openfy.features.community.sync.LocalShareServer { *; }
-keepclassmembers class com.example.openfy.features.community.sync.LocalShareServer { *; }
-keep class com.example.openfy.features.community.sync.SharePayload { *; }
-keepclassmembers class com.example.openfy.features.community.sync.SharePayload { *; }

# ------------------------------------------------------------------------------
# 3. Jetpack Compose & Custom UI Components
# ------------------------------------------------------------------------------
-keep class androidx.compose.** { *; }
-keep class **.openfy.core.ui.components.** { *; }
-keep class **.openfy.ui.components.** { *; }
-keep class io.github.artiomitprograming.openfy.core.ui.components.** { *; }
-keep class io.github.artiomitprograming.openfy.ui.components.** { *; }
-keep class com.example.openfy.core.ui.components.** { *; }
-keep class com.example.openfy.ui.components.** { *; }

# ------------------------------------------------------------------------------
# 4. QR Scanner (CameraX & ZXing)
# ------------------------------------------------------------------------------
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**

# ------------------------------------------------------------------------------
# 5. Media3, ExoPlayer & DSP Audio Services
# ------------------------------------------------------------------------------
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**
-keep class **.openfy.core.audio.service.** { *; }
-keepclassmembers class **.openfy.core.audio.service.** { *; }
-keep class io.github.artiomitprograming.openfy.core.audio.service.** { *; }
-keepclassmembers class io.github.artiomitprograming.openfy.core.audio.service.** { *; }
-keep class com.example.openfy.core.audio.service.** { *; }
-keepclassmembers class com.example.openfy.core.audio.service.** { *; }

# ------------------------------------------------------------------------------
# 6. Image Loading (Coil) & Networking (OkHttp/Okio)
# ------------------------------------------------------------------------------
-dontwarn coil.**
-keep class coil.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# ------------------------------------------------------------------------------
# 7. AndroidX Credentials & Security (Clean FOSS / No Google Play Services)
# ------------------------------------------------------------------------------
-keep class androidx.credentials.** { *; }
-dontwarn androidx.credentials.**
-keep class androidx.security.crypto.** { *; }

# ------------------------------------------------------------------------------
# 8. Preferences & JSON Fallbacks
# ------------------------------------------------------------------------------
-keep class androidx.datastore.** { *; }
-keep class com.google.code.gson.** { *; }
-keep class com.google.gson.** { *; }
