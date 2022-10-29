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
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    implementation("androidx.core:core-ktx:1.9.0@aar")
    implementation("androidx.annotation:annotation:1.5.0")

    implementation("androidx.appcompat:appcompat:1.6.0-rc01")
    implementation("com.google.android.material:material:1.7.0")
    implementation("androidx.fragment:fragment-ktx:1.5.3")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.preference:preference-ktx:1.2.0")

    /* Dependency Injection */
    implementation("com.google.dagger:dagger:2.41")
    kapt("com.google.dagger:dagger-compiler:2.41")

    /* Testing */
    testImplementation("org.hamcrest:hamcrest-library:$hamcrestVersion")
    testImplementation("junit:junit:$junitVersion")
    testImplementation("org.robolectric:robolectric:$robolectricVersion")
    testImplementation("org.mockito:mockito-core:$mockitoVersion")
    testImplementation("org.mockito:mockito-inline:$mockitoVersion")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    testImplementation("androidx.test:core-ktx:1.4.0")
}