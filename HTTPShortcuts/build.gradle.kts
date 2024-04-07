buildscript {
    repositories {
        mavenCentral()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.3.1")
        classpath("com.bugsnag:bugsnag-android-gradle-plugin:8.1.0")
        classpath("org.jetbrains:markdown:0.6.1")
        classpath(kotlin("gradle-plugin", "1.9.23"))
        classpath("com.github.bjoernq:unmockplugin:0.7.9")
    }
}

plugins {
    id("com.diffplug.spotless") version "6.4.2"
    id("org.jetbrains.kotlin.jvm") version "1.9.23" apply false
    id("io.realm.kotlin") version "1.14.0" apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
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
