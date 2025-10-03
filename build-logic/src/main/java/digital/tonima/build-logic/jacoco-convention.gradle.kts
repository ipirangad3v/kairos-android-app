plugins {
    id("jacoco")
    id("com.android.base")
}
tasks.register<JacocoReport>("createDebugCoverageReport") {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val fileFilter =
        listOf( // Android
            "**/R.class",
            "**/R$*.class",
            "**/BuildConfig.*",
            "**/Manifest*.*",
            "**/*Test*.*",
            "android/**/*.*", // DI, generated code
            "**/*_Hilt*.class",
            "**/Dagger*Component.class",
            "**/Dagger*Module.class",
            "**/Dagger*Module_Provide*Factory.class",
            "**/*_Provide*Factory*.*",
            "**/*_Factory*.*",
        )

    val debugTree =
        fileTree("${layout.buildDirectory}/tmp/kotlin-classes/debug") { exclude(fileFilter) }
    val mainSrc = "${project.projectDir}/src/main/java"

    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(
        fileTree(layout.buildDirectory) {
            include("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
        },
    )
}
