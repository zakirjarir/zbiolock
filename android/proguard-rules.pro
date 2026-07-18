# ──────────────────────────────────────────────────
# ZBioLock ProGuard / R8 Rules
# ──────────────────────────────────────────────────

# Keep all plugin classes so Capacitor can reflect on them at runtime
-keep class com.zakirjarir.zbiolock.** { *; }

# Keep Capacitor bridge infrastructure
-keep class com.getcapacitor.** { *; }
-keepclassmembers class * extends com.getcapacitor.Plugin {
    @com.getcapacitor.annotation.CapacitorPlugin <methods>;
    @com.getcapacitor.PluginMethod <methods>;
}

# Keep AndroidX Biometric classes used via reflection
-keep class androidx.biometric.** { *; }

# Keep Jetpack Security / EncryptedSharedPreferences
-keep class androidx.security.crypto.** { *; }

# Kotlin metadata (required for reflection-based Kotlin features)
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
