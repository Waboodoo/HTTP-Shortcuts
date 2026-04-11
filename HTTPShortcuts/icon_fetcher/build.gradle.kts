plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    implementation(libs.okhttp3)
    implementation(libs.gson)
    implementation(libs.kotlinx.coroutines)
}
