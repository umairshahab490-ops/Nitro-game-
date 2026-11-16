# Nitro Rush ProGuard / R8 Keep Rules

-keepattributes SourceFile,LineNumberTable
-keep class org.osmdroid.** { *; }
-dontwarn org.osmdroid.**
-keep class com.google.android.gms.location.** { *; }
