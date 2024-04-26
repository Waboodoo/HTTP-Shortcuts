plugins {
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    implementation(libs.okhttp3)
    implementation(libs.gson)
    implementation(libs.kotlinx.coroutines)
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}