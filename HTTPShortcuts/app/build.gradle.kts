plugins {
    id("de.jansauer.poeditor") version "1.1.0"
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("com.bugsnag.android.gradle")
    id("realm-android")
}

val kotlinVersion: String by properties
val hamcrestVersion: String by properties
val junitVersion: String by properties
val robolectricVersion: String by properties
val bugsnagAPIKey: String by rootProject.ext
val poeditorAPIKey: String by rootProject.ext
val poeditorProjectId: String by rootProject.ext

android {
    namespace = "ch.rmy.android.http_shortcuts"

    compileSdk = 33

    kotlinOptions {
        languageVersion = "1.6"
    }

    lint {
        disable.add("MissingTranslation")
    }

    defaultConfig {
        applicationId = "ch.rmy.android.http_shortcuts"
        minSdk = 21
        targetSdk = 33

        // Version name and code must remain as literals so that F-Droid can read them
        versionName = "2.19.0"
        // 11,(2 digits major),(2 digits minor),(2 digits patch),(2 digits build)
        versionCode = 1102190002

        buildConfigField("String", "BUGSNAG_API_KEY", "\"$bugsnagAPIKey\"")
        buildConfigField("String", "BUILD_TIMESTAMP", "\"${rootProject.ext["buildTimestamp"]}\"")

        manifestPlaceholders["bugsnagAPIKey"] = bugsnagAPIKey
        testInstrumentationRunnerArguments["package"] = "ch.rmy.android.http_shortcuts"
        vectorDrawables.useSupportLibrary = true

        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
        }
    }

    buildTypes {
        /* Used for development & testing */
        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false

            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            applicationIdSuffix = ".debug"
        }

        /* Used for F-Droid */
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }

        /* Used for Play Store & Github release page */
        create("releaseFull") {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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

    bundle {
        language {
            enableSplit = false
        }
        density {
            enableSplit = true
        }
        abi {
            enableSplit = true
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

    packagingOptions {
        jniLibs {
            excludes.add("META-INF/*")
        }
        resources {
            excludes.add("META-INF/*")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    sourceSets.getByName("main") {
        java.setSrcDirs(listOf("src/main/kotlin"))
    }
    sourceSets.getByName("test") {
        java.setSrcDirs(listOf("src/test/kotlin"))
    }
    sourceSets.getByName("debug") {
        java.setSrcDirs(listOf("src/withoutCrashLogging/kotlin", "src/withGoogleServices/kotlin"))
    }
    sourceSets.getByName("release") {
        java.setSrcDirs(listOf("src/withoutCrashLogging/kotlin", "src/withoutGoogleServices/kotlin"))
    }
    sourceSets.getByName("releaseFull") {
        java.setSrcDirs(listOf("src/withCrashLogging/kotlin", "src/withGoogleServices/kotlin"))
    }
}

if (bugsnagAPIKey.isNotEmpty()) {
    bugsnag {
        uploadJvmMappings.set(false)
        uploadNdkMappings.set(false)
        uploadNdkUnityLibraryMappings.set(false)
        reportBuilds.set(false)
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")

    /* Dependency Injection */
    implementation("com.google.dagger:dagger:2.41")
    kapt("com.google.dagger:dagger-compiler:2.41")

    /* Support libraries */
    implementation("androidx.core:core-ktx:1.8.0@aar")
    implementation("androidx.appcompat:appcompat:1.6.0-alpha05")
    implementation("androidx.fragment:fragment-ktx:1.4.1")
    implementation("com.google.android.material:material:1.6.1")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.annotation:annotation:1.4.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.1")
    implementation("android.arch.lifecycle:extensions:1.1.1")
    implementation("android.arch.lifecycle:viewmodel:1.1.1")
    implementation("androidx.core:core-splashscreen:1.0.0-rc01")

    /* Dialogs / Activities */
    implementation("com.afollestad.material-dialogs:core:3.3.0")
    implementation("com.afollestad.material-dialogs:input:3.3.0")
    implementation("com.github.skydoves:colorpickerview:2.2.4")

    /* Animated progress bar */
    implementation("com.github.castorflex.smoothprogressbar:library:1.1.0")
    implementation("com.github.castorflex.smoothprogressbar:library-circular:1.3.0")

    /* Image cropping */
    implementation("com.github.yalantis:ucrop:2.2.8")

    /* Image display */
    implementation("com.squareup.picasso:picasso:2.71828")

    /* HTTP & Network */
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("io.github.rburgst:okhttp-digest:2.6")
    implementation("com.github.franmontiel:PersistentCookieJar:v1.0.1")
    implementation("org.conscrypt:conscrypt-android:2.5.2")

    /* Scheduling */
    implementation("androidx.work:work-runtime-ktx:2.7.1")
    implementation("androidx.work:work-rxjava2:2.7.1")

    /* Tasker integration */
    implementation("com.joaomgcd:taskerpluginlibrary:0.4.2")

    /* Scripting */
    implementation("com.github.LiquidPlayer:LiquidCore:0.6.2")

    /* Location lookup (for Scripting) */
    debugImplementation("com.google.android.gms:play-services-location:20.0.0")
    "releaseFullImplementation"("com.google.android.gms:play-services-location:20.0.0")

    /* Password hashing */
    implementation("org.mindrot:jbcrypt:0.4")

    /* Crash Reporting */
    "releaseFullImplementation"("com.bugsnag:bugsnag-android:5.23.1")

    /* cURL import & export */
    implementation(project(path = ":curl_command"))

    /* Favicon fetching */
    implementation(project(path = ":favicon_grabber"))

    /* JSON serialization & deserialization */
    implementation("com.google.code.gson:gson:2.8.9")

    /* RX */
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation("com.github.tbruyelle:rxpermissions:0.11")
    implementation("com.victorrendina:rxqueue2:2.0.0")

    /* Testing */
    testImplementation("org.hamcrest:hamcrest-library:$hamcrestVersion")
    testImplementation("junit:junit:$junitVersion")
    testImplementation("org.robolectric:robolectric:$robolectricVersion")
    testImplementation("org.robolectric:shadows-multidex:$robolectricVersion")
    testImplementation("androidx.test:monitor:1.5.0")
}

poeditor {
    apiKey.set(poeditorAPIKey)
    projectId.set(poeditorProjectId)

    terms(mapOf(
        "lang" to "en",
        "file" to "src/main/res/values/strings.xml",
        "updating" to "terms_translations",
        "overwrite" to true,
        "sync_terms" to true,
    ))
    // translation definitions omitted as the plugin currently does not support filtering by "translated" status, making its pull feature unusable
}
