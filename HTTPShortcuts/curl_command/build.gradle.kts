plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    testImplementation(libs.kotlin.test.junit5)
}

tasks {
    test {
        useJUnitPlatform()
    }
}
