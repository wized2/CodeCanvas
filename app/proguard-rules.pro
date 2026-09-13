# Keep Compose and reflection-related
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep our package
-keep class com.endroid.code.** { *; }

# General Android
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
