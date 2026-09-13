# CodeCanvas v2 – lean R8 rules
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

-keep class com.endroid.code.MainActivity
-keep class com.endroid.code.viewmodel.EditorViewModel

-keep class androidx.compose.runtime.** { *; }

-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
}

-dontwarn kotlin.**
-dontwarn org.jetbrains.annotations.**
