plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
}

val kotlinVersion: String by properties
val kotlinTestJunit5Version: String by properties

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:$kotlinTestJunit5Version")

}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    test {
        useJUnitPlatform()
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}