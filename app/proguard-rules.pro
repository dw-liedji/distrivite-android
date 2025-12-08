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

# Keep TensorFlow Lite GPU Delegate classes
-keep class org.tensorflow.lite.gpu.** { *; }
-keepclassmembers class org.tensorflow.lite.gpu.** { *; }
-dontwarn org.tensorflow.lite.gpu.**

#keep Authentication classes
-keep class com.datavite.distrivite.core.data.remote.model.** { *; }
-keep class com.datavite.distrivite.feature.cameis.data.remote.model.** { *; }

# Keep generic type information for Gson/Moshi serialization
#-keepattributes Signature
#-keepattributes RuntimeVisibleAnnotations

# Keep Retrofit service interfaces
-keep interface * { @retrofit2.http.* <methods>; }
# Keep all classes related to Retrofit and Gson/Moshi
-keep class retrofit2.** { *; }
# Don't warn about missing classes that Retrofit or Gson may reference
-dontwarn okhttp3.**
-dontwarn retrofit2.**

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile