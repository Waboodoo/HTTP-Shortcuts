import com.android.build.api.dsl.LibraryExtension

plugins {
    id("com.android.library")
}

extensions.configure<LibraryExtension> {
    namespace = "ch.rmy.android.scripting"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        /* Used for F-Droid */
        release {
            ndk.debugSymbolLevel = "SYMBOL_TABLE"
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }

        /* Used for Play Store & GitHub release page */
        create("releaseFull") {
            ndk.debugSymbolLevel = "SYMBOL_TABLE"
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.quickJsWrapper)

    testImplementation(libs.kotlin.test.junit5)
}
