# Project ProGuard rules for NotiFast Release Builds

# Preserve line numbers and source files for Crashlytics stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Firebase General
-keepattributes Signature
-keepattributes *Annotation*

# Firebase Crashlytics
-keepattributes *Annotation*,InnerClasses
-dontwarn com.google.firebase.crashlytics.**
-keep class com.google.firebase.crashlytics.** { *; }

# Firebase Analytics & GMS
-keep class com.google.android.gms.internal.measurement.** { *; }
-keep class com.google.firebase.analytics.** { *; }

# Firestore data models - keep all fields for POJO serialization
-keep class com.notifast.app.data.model.** { *; }
-keepclassmembers class com.notifast.app.data.model.** {
    <fields>;
    <init>(...);
}

# Hilt & Dagger
-dontwarn dagger.hilt.**
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel

# Coil Image Loader
-dontwarn coil.**
-keep class coil.** { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# Jetpack Compose
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }