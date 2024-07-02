plugins {
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    implementation(libs.okhttp3)
    implementation(libs.gson)
    implementation(libs.kotlinx.coroutines)
}
