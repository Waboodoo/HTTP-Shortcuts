plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("org.jetbrains.kotlin.android")
    id("de.mobilej.unmock")
}

android {
    namespace = "ch.rmy.android.framework"
    compileSdk = 34

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        /* Used for development & testing */
        getByName("debug") {
            isMinifyEnabled = false
        }

        /* Used for F-Droid */
        getByName("release") {
            isMinifyEnabled = true
        }

        /* Used for Play Store & Github release page */
        create("releaseFull") {
            isMinifyEnabled = true
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("x86", "x86_64", "armeabi-v7a", "arm64-v8a")
            isUniversalApk = true
        }
    }

    buildFeatures {
        buildConfig = true
        viewBinding = false
        dataBinding = false
        aidl = false
        renderScript = false
        resValues = false
        shaders = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
}

unMock {
    keep("android.net.Uri")
}

dependencies {
    coreLibraryDesugaring(libs.desugar)
    api(libs.kotlin.stdlib)
    api(libs.kotlinx.coroutines.android)
    api(libs.androidx.core)
    api(libs.androidx.annotation)
    api(libs.androidx.appcompat)
    api(libs.androidx.preference)

    /* Dependency Injection */
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)

    /* Database */
    api(libs.realm)

    /* Testing */
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.mockk)
    testImplementation(libs.androidx.test)
    testImplementation(libs.kotlinx.coroutines.test)
}

kapt {
    correctErrorTypes = true
}