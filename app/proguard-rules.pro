# CodeCanvas – aggressive but safe rules for a small editor APK

-keepattributes *Annotation*,SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

-keep class com.endroid.code.MainActivity { *; }
-keep class com.endroid.code.viewmodel.EditorViewModel { *; }

-dontwarn androidx.compose.**
-keep class androidx.compose.runtime.** { *; }

-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }

-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
