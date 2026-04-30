# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keepclassmembers class mil.nga.** { *; }
-keep class mil.nga.** { *; }
-dontwarn mil.nga.**

-keepclassmembers class com.j256.ormlite.** { *; }
-keep class com.j256.ormlite.** { *; }
-dontwarn com.j256.ormlite.**

-keepclassmembers class cscs.** { *; }
-keep class cscs.** { *; }
-dontwarn cscs.**

-keep class org.locationtech.proj4j.** { *; }
-dontwarn org.locationtech.proj4j.**

-keepattributes Signature, EnclosingMethod, AnnotationDefault, *Annotation*

-keep class org.sqlite.** { *; }
-keepclassmembers class org.sqlite.** { *; }
-dontwarn org.sqlite.**