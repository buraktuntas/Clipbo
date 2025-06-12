# Clipbo ProGuard Rules

# Preserve line numbers for debugging crashes
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep all classes in the main package
-keep class com.bt.clipbo.** { *; }

# Room Database
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-dontwarn androidx.room.paging.**

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp
-keepclasseswithmembers class * {
    @dagger.hilt.android.AndroidEntryPoint <methods>;
}

# Jetpack Compose
-keep class androidx.compose.** { *; }
-keep class kotlin.Metadata { *; }
-keepclassmembers class androidx.compose.** {
    <init>(...);
}

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keep,includedescriptorclasses class com.bt.clipbo.**$$serializer { *; }
-keepclassmembers class com.bt.clipbo.** {
    *** Companion;
}
-keepclasseswithmembers class com.bt.clipbo.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Android Keystore
-keep class android.security.keystore.** { *; }
-keep class java.security.** { *; }
-keep class javax.crypto.** { *; }

# Biometric
-keep class androidx.biometric.** { *; }

# Accessibility Service
-keep class * extends android.accessibilityservice.AccessibilityService {
    <init>(...);
    public <methods>;
}

# Widget/Glance
-keep class androidx.glance.** { *; }
-keep class * extends androidx.glance.appwidget.GlanceAppWidget
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver

# AdMob (if using)
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Parcelable
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# Remove Timber logging in release
-assumenosideeffects class timber.log.Timber {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# Keep BuildConfig
-keep class com.bt.clipbo.BuildConfig { *; }

# Database entities and DAOs
-keep class com.bt.clipbo.data.database.** { *; }

# Domain models
-keep class com.bt.clipbo.domain.model.** { *; }

# Backup/Restore data classes
-keep class com.bt.clipbo.utils.BackupData { *; }
-keep class com.bt.clipbo.utils.BackupClipboardItem { *; }
-keep class com.bt.clipbo.utils.BackupTag { *; }
-keep class com.bt.clipbo.utils.BackupMetadata { *; }

# Widget data classes
-keep class com.bt.clipbo.widget.WidgetClipboardItem { *; }

# Preferences
-keep class com.bt.clipbo.data.preferences.** { *; }

# Error handling
-keep class com.bt.clipbo.utils.ClipboError { *; }
-keep class com.bt.clipbo.utils.ClipboError$** { *; }

# Analytics
-keep class com.bt.clipbo.utils.AnalyticsEvent { *; }
-keep class com.bt.clipbo.utils.AnalyticsEvent$** { *; }

# Generic optimizations
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Remove unused resources
-dontwarn javax.annotation.**
-dontwarn org.jetbrains.annotations.**