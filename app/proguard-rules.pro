# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep Gson TypeToken
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Keep Room entities
-keep class com.teabiz.crm.data.model.** { *; }

# Keep Apache POI
-keep class org.apache.poi.** { *; }
-dontwarn org.apache.poi.**

# Keep OpenCSV
-keep class com.opencsv.** { *; }
-dontwarn com.opencsv.**

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel

# Keep data classes for serialization
-keep class com.teabiz.crm.data.remote.** { *; }

# Gemini AI
-keep class com.google.ai.client.generativeai.** { *; }
-dontwarn com.google.ai.client.generativeai.**
