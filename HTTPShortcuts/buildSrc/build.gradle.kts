plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

repositories {
    google()
    maven("https://plugins.gradle.org/m2/")
}

dependencies {
    implementation(libs.okhttp3)
    implementation(libs.gson)
    implementation(libs.minifyHtml)
    implementation(libs.opencsv)
    implementation(libs.jetbrains.markdown)
}
