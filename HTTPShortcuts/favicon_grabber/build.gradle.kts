plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
}

val coroutinesVersion: String by properties
val gsonVersion: String by properties
val okHttpVersion: String by properties

dependencies {
    implementation("com.squareup.okhttp3:okhttp:$okHttpVersion")
    implementation("com.google.code.gson:gson:$gsonVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
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