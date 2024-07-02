buildscript {
    repositories {
        mavenCentral()
        google()
    }
    dependencies {
        classpath(libs.kotlin.gradle.plugin)
        classpath(libs.android.gradle)
        classpath(libs.bugsnag.gradle)
        classpath(libs.jetbrains.markdown)
        classpath(libs.unmock)
    }
}

plugins {
    alias(libs.plugins.spotless)
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.realm) apply false
    alias(libs.plugins.hiltAndroid) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.compose.compiler) apply false
}

ext {
    set("bugsnagAPIKey", System.getenv("HTTP_SHORTCUTS_BUGSNAG_API_KEY") ?: "")
    set("autoBuildDocs", System.getenv("HTTP_SHORTCUTS_AUTO_BUILD_DOCS") == "true")
    set("buildTimestamp", java.util.Date().time.toString())
}

allprojects {
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        google()
    }

    apply {
        plugin("com.diffplug.spotless")
    }

    spotless {
        kotlin {
            target(fileTree(".") {
                include("**/*.kt")
            })
            ktlint("0.43.2").userData(mapOf(
                "max_line_length" to "150",
                "indent_size" to "4",
                "insert_final_newline" to "true",
            ))
        }
    }
}
