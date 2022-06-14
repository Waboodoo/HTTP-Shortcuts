plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
}

val junitVersion: String by properties
val kotlinVersion: String by properties

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")

    testImplementation("junit:junit:$junitVersion")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}