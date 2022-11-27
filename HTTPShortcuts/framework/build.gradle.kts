plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("org.jetbrains.kotlin.android")
    id("realm-android")
}

val kotlinVersion: String by properties
val coroutinesVersion: String by properties
val hamcrestVersion: String by properties
val junitVersion: String by properties
val robolectricVersion: String by properties
val mockitoVersion: String by properties

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
}

dependencies {
    api("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    api("androidx.core:core-ktx:1.9.0@aar")
    api("androidx.annotation:annotation:1.5.0")
    api("androidx.appcompat:appcompat:1.6.0-rc01")
    api("com.google.android.material:material:1.7.0")
    api("androidx.fragment:fragment-ktx:1.5.4")
    api("androidx.recyclerview:recyclerview:1.2.1")
    api("androidx.constraintlayout:constraintlayout:2.1.4")
    api("androidx.preference:preference-ktx:1.2.0")

    /* Dependency Injection */
    api("com.google.dagger:dagger:2.41")
    kapt("com.google.dagger:dagger-compiler:2.41")

    /* Testing */
    testApi("org.hamcrest:hamcrest-library:$hamcrestVersion")
    testApi("junit:junit:$junitVersion")
    testApi("org.robolectric:robolectric:$robolectricVersion")
    testApi("org.mockito:mockito-core:$mockitoVersion")
    testApi("org.mockito:mockito-inline:$mockitoVersion")
    testApi("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    testApi("androidx.test:core-ktx:1.5.0")
}