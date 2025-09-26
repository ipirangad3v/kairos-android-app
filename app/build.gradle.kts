import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.spotless)
    alias(libs.plugins.google.services)
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.ksp)
}

android {
    namespace = "digital.tonima.kairos"
    compileSdk = rootProject.extra["COMPILE_SDK_VERSION"].toString().toInt()

    defaultConfig {
        applicationId = "digital.tonima.kairos"
        minSdk = rootProject.extra["MIN_SDK_VERSION"].toString().toInt()
        targetSdk = rootProject.extra["TARGET_SDK_VERSION"].toString().toInt()
        versionCode = rootProject.extra["APP_VERSION_CODE"].toString().toInt()
        versionName = rootProject.extra["APP_VERSION_NAME"].toString()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            val admobAppIdTest = "ca-app-pub-3940256099942544~3347511713"
            val admobBannerAdUnitIdTest = "ca-app-pub-3940256099942544/6300978111"

            resValue("string", "admob_app_id", admobAppIdTest)
            buildConfigField("String", "ADMOB_BANNER_AD_UNIT_HOME", "\"$admobBannerAdUnitIdTest\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

            val isRunningReleaseTask =
                gradle.startParameter.taskNames.any { it.contains("release", ignoreCase = true) }

            val admobAppIdTest = "ca-app-pub-3940256099942544~3347511713"
            val admobBannerAdUnitIdTest = "ca-app-pub-3940256099942544/6300978111"

            val admobAppId: String
            val admobBannerAdUnitIdHome: String

            if (isRunningReleaseTask) {
                // Se for uma tarefa de release, carrega os segredos reais
                val localProperties = Properties()
                val localPropertiesFile = rootProject.file("local.properties")
                if (localPropertiesFile.exists()) {
                    localProperties.load(FileInputStream(localPropertiesFile))
                }

                admobAppId =
                    System.getenv("ADMOB_APP_ID")
                        ?: localProperties.getProperty("admob.app.id")
                            ?: admobAppIdTest

                admobBannerAdUnitIdHome =
                    System.getenv("ADMOB_BANNER_AD_UNIT_HOME")
                        ?: localProperties.getProperty("admob.banner.ad.unit.home")
                            ?: admobBannerAdUnitIdTest
            } else {
                // Para qualquer outra tarefa (como spotlessCheck), usa os IDs de teste
                admobAppId = admobAppIdTest
                admobBannerAdUnitIdHome = admobBannerAdUnitIdTest
            }

            resValue("string", "admob_app_id", admobAppId)
            buildConfigField("String", "ADMOB_BANNER_AD_UNIT_HOME", "\"$admobBannerAdUnitIdHome\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlin { jvmToolchain(21) }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(platform(libs.google.firebase.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.calendar)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.play.services.ads.api)
    implementation(libs.accompanist.permissions)
    implementation(libs.google.firebase.analytics)

    // hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.binder)
    implementation(libs.hilt.worker)
    ksp(libs.hilt.binder.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

apply(from = "../spotless.gradle")
