# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Gson 免混淆规则 - 保护 JSON 实体类
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.stream.** { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# 保留 JSON 实体类的字段名
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# 保留数据类所有字段（Gson 通过反射获取字段名）
-keep class com.jizhi.data.remote.** { *; }
-keep class com.jizhi.data.local.** { *; }

# Retrofit 免混淆规则
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# OkHttp 免混淆规则
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**

# Room 免混淆规则
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Hilt 免混淆规则
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ComponentSupplier { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Kotlin 序列化
-keepattributes InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Jetpack Compose 保护规则（防止 ProGuard 混淆导致崩溃）
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
-keepclassmembers class androidx.compose.** { *; }

# 保留 Compose @Composable 注解
-keepattributes *Annotation*
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations

# 保留 Compose 的 Composition 相关类
-keep class androidx.compose.runtime.** { *; }
-keepclassmembers class androidx.compose.runtime.** { *; }

# 保留 Hilt 与 Compose 相关的类
-keep class dagger.hilt.android.internal.lifecycle.** { *; }
-keepclassmembers class dagger.hilt.android.internal.lifecycle.** { *; }

# Android AppWidget 保护规则（必须保留，否则小组件无法工作）
-keep class * extends android.appwidget.AppWidgetProvider { *; }
-keep class com.jizhi.widget.SentenceWidgetProvider { *; }
-keep class com.jizhi.widget.SentenceWidgetService { *; }
-keep class com.jizhi.widget.SentenceWidgetViewsFactory { *; }

# 保留 AppWidgetProvider 的 onReceive 方法
-keepclassmembers class * extends android.appwidget.AppWidgetProvider {
    public void onReceive(android.content.Context, android.content.Intent);
    public void onUpdate(android.content.Context, android.appwidget.AppWidgetManager, int[]);
    public void onDeleted(android.content.Context, int[]);
    public void onEnabled(android.content.Context);
    public void onDisabled(android.content.Context);
}

# 保留 RemoteViews 相关类
-keep class android.widget.RemoteViews { *; }
-dontwarn android.widget.RemoteViews

# 保留 Intent 和 Bundle 相关类（用于小组件通信）
-keep class android.content.Intent { *; }
-keep class android.os.Bundle { *; }
-keep class android.app.PendingIntent { *; }

# 如果项目使用 WebView with JS，取消注释以下规则
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# 保留行号信息用于调试堆栈跟踪
-keepattributes SourceFile,LineNumberTable

# 如果保留行号信息，取消注释以下规则以隐藏原始源文件名
#-renamesourcefileattribute SourceFile
