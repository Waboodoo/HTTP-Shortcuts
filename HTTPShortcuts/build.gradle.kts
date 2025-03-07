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
        classpath(libs.minifyHtml)
        classpath(libs.unmock)
    }
}

plugins {
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.realm) apply false
    alias(libs.plugins.hiltAndroid) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.compose.compiler) apply false
}

allprojects {
    apply {
        plugin("org.jlleitschuh.gradle.ktlint")
    }
}
