plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("org.jetbrains.kotlin.android")
    id("de.mobilej.unmock")
}

val kotlinVersion: String by properties
val coroutinesVersion: String by properties
val kotlinTestJunit5Version: String by properties
val mockkVersion: String by properties
val androidCoreKtxTestVersion: String by properties
val hiltVersion: String by properties

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
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    api("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    api("androidx.core:core-ktx:1.12.0@aar")
    api("androidx.annotation:annotation:1.7.1")
    api("androidx.appcompat:appcompat:1.6.1")
    api("androidx.preference:preference-ktx:1.2.1")

    /* Dependency Injection */
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    kapt("com.google.dagger:hilt-android-compiler:$hiltVersion")

    /* Database */
    api("io.realm.kotlin:library-base:1.15.0")

    /* Testing */
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:$kotlinTestJunit5Version")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("androidx.test:core-ktx:$androidCoreKtxTestVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
}

kapt {
    correctErrorTypes = true
}