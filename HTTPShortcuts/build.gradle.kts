buildscript {
    repositories {
        mavenCentral()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.3.1")
        classpath("io.realm:realm-gradle-plugin:10.8.1")
        classpath("com.bugsnag:bugsnag-android-gradle-plugin:7.2.1")
        classpath("org.jetbrains:markdown:0.3.5")
        classpath(kotlin("gradle-plugin", "1.7.10"))
    }
}

plugins {
    id("com.diffplug.spotless") version "6.4.2"
    id("org.jetbrains.kotlin.jvm") version "1.7.10" apply false
}

ext {
    set("bugsnagAPIKey", System.getenv("HTTP_SHORTCUTS_BUGSNAG_API_KEY") ?: "")
    set("poeditorAPIKey", System.getenv("HTTP_SHORTCUTS_PO_EDITOR_API_KEY") ?: "")
    set("poeditorProjectId", System.getenv("HTTP_SHORTCUTS_PO_EDITOR_PROJECT_ID") ?: "")
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
