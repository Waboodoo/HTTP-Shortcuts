plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("org.jetbrains.kotlin.android")
    id("realm-android")
    id("de.mobilej.unmock")
}

val kotlinVersion: String by properties
val coroutinesVersion: String by properties
val kotlinTestJunit5Version: String by properties
val mockkVersion: String by properties
val androidCoreKtxTestVersion: String by properties

android {
    namespace = "ch.rmy.android.framework"
    compileSdk = 33

    defaultConfig {
        minSdk = 21
        targetSdk = 33

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
        viewBinding = true
        dataBinding = false
        aidl = false
        renderScript = false
        resValues = false
        shaders = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        languageVersion = "1.6"
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
    api("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    api("androidx.core:core-ktx:1.9.0@aar")
    api("androidx.annotation:annotation:1.5.0")
    api("androidx.appcompat:appcompat:1.6.0-rc01")
    api("com.google.android.material:material:1.7.0")
    api("androidx.fragment:fragment-ktx:1.5.5")
    api("androidx.recyclerview:recyclerview:1.2.1")
    api("androidx.constraintlayout:constraintlayout:2.1.4")
    api("androidx.preference:preference-ktx:1.2.0")

    /* Dependency Injection */
    api("com.google.dagger:dagger:2.41")
    kapt("com.google.dagger:dagger-compiler:2.41")

    /* Testing */
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:$kotlinTestJunit5Version")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("androidx.test:core-ktx:$androidCoreKtxTestVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
}