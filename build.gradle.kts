// Top-level build file where you can add configuration options common to all sub-projects/modules.
val appVersionCode = project.findProperty("APP_VERSION_CODE") ?: "1"
val wearAppVersionCode = project.findProperty("WEAR_APP_VERSION_CODE") ?: "1"
val appVersionName = project.findProperty("APP_VERSION_NAME") ?: "1.0.0"

val minSdkVersion = project.findProperty("MIN_SDK_VERSION")?.toString()?.toInt() ?: 30
val targetSdkVersion = project.findProperty("TARGET_SDK_VERSION")?.toString()?.toInt() ?: 36
val compileSdkVersion = project.findProperty("COMPILE_SDK_VERSION")?.toString()?.toInt() ?: 36

extra.set("APP_VERSION_CODE", appVersionCode)
extra.set("WEAR_APP_VERSION_CODE", wearAppVersionCode)
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
    alias(libs.plugins.jacoco.convention) apply false
    id("jacoco")
}

tasks.register<JacocoReport>("createJacocoMergedCoverageReport") {
    group = "Reporting"
    description = "Generates a merged Jacoco code coverage report for all modules."

    val modulesToInclude = listOf(
        ":app",
        ":core",
        ":wear"
    )

    dependsOn(modulesToInclude.map { "$it:createJacocoDebugCoverageReport" })

    sourceDirectories.setFrom(files(subprojects.flatMap {
        listOf("${it.projectDir}/src/main/java", "${it.projectDir}/src/main/kotlin")
    }))

    classDirectories.setFrom(files(subprojects.flatMap { sp ->
        listOf(
            fileTree("${sp.buildDir}/tmp/kotlin-classes/debug") {
                exclude(
                    "**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*",
                    "**/*Test*.*", "android/**/*.*", "**/*_Hilt*.class", "**/Dagger*Component.class",
                    "**/Dagger*Module.class", "**/Dagger*Module_Provide*Factory.class",
                    "**/*_Provide*Factory*.*", "**/*_Factory*.*"
                )
            },
            fileTree("${sp.buildDir}/intermediates/javac/debug/classes") {
                exclude(
                    "**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*",
                    "**/*Test*.*", "android/**/*.*", "**/*_Hilt*.class", "**/Dagger*Component.class",
                    "**/Dagger*Module.class", "**/Dagger*Module_Provide*Factory.class",
                    "**/*_Provide*Factory*.*", "**/*_Factory*.*"
                )
            }
        )
    }))

    executionData.setFrom(files(subprojects.flatMap { sp ->
        listOf(
            fileTree(sp.buildDir) { include("jacoco/testDebugUnitTest.exec") },
            fileTree(sp.buildDir) { include("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec") }
        )
    }))

    reports {
        xml.required.set(true)
        xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/createJacocoMergedCoverageReport/createJacocoMergedCoverageReport.xml"))
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/createJacocoMergedCoverageReport/html"))
    }
}
