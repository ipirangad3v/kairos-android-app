// Top-level build file where you can add configuration options common to all sub-projects/modules.
val appVersionCode = project.findProperty("APP_VERSION_CODE") ?: "1"
val appVersionName = project.findProperty("APP_VERSION_NAME") ?: "1.0.0"

val minSdkVersion = project.findProperty("MIN_SDK_VERSION")?.toString()?.toInt() ?: 30
val targetSdkVersion = project.findProperty("TARGET_SDK_VERSION")?.toString()?.toInt() ?: 36
val compileSdkVersion = project.findProperty("COMPILE_SDK_VERSION")?.toString()?.toInt() ?: 36

extra.set("APP_VERSION_CODE", appVersionCode)
extra.set("APP_VERSION_NAME", appVersionName)
extra.set("MIN_SDK_VERSION", minSdkVersion)
extra.set("TARGET_SDK_VERSION", targetSdkVersion)
extra.set("COMPILE_SDK_VERSION", compileSdkVersion)

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.crashlytics) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.kotlin.ksp) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
}
