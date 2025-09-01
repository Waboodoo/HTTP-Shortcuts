import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.internal.api.BaseVariantOutputImpl

plugins {
    id("com.android.application")
    id("kotlin-android")
    alias(libs.plugins.ksp)
    id("io.realm.kotlin")
    id("de.mobilej.unmock")
    id("com.google.dagger.hilt.android")
    alias(libs.plugins.compose.compiler)
}

val buildTimestamp = System.currentTimeMillis()

android {
    namespace = "ch.rmy.android.http_shortcuts"

    compileSdk = 36

    kotlinOptions {
        jvmTarget = "11"
    }

    lint {
        disable.add("MissingTranslation")
        disable.add("Instantiatable")
    }

    defaultConfig {
        applicationId = "ch.rmy.android.http_shortcuts.restore"
        minSdk = 26
        targetSdk = 35

        // Version name and code must remain as literals so that F-Droid can read them
        versionName = "1.0.0"
        // 11,(2 digits major),(2 digits minor),(2 digits patch),(2 digits build)
        versionCode = 1

        buildConfigField("long", "BUILD_TIMESTAMP", buildTimestamp.toString())

        testInstrumentationRunnerArguments["package"] = "ch.rmy.android.http_shortcuts.restore"
        vectorDrawables.useSupportLibrary = true

        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
        }
    }

    androidResources {
        localeFilters += setOf(
            "en",
            "de",
        )
    }

    signingConfigs {
        create("development") {
            keyAlias = "development"
            keyPassword = "Password1"
            storePassword = "Password1"
            storeFile = file("../keystores/development.jks")
        }
    }

    buildTypes {
        /* Used for development & testing */
        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false

            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs["development"]
        }

        /* Used for F-Droid */
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    val isBuildingBundle = gradle.startParameter.taskNames.any { it.contains("bundle", ignoreCase = true) }
    splits {
        abi {
            isEnable = !isBuildingBundle
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
        compose = true
        buildConfig = true
        viewBinding = false
        dataBinding = false
        aidl = false
        renderScript = false
        resValues = false
        shaders = false
    }

    packaging {
        jniLibs {
            excludes.add("META-INF/*")
            useLegacyPackaging = true
        }
        resources {
            excludes.add("META-INF/*")

            // See https://github.com/Kotlin/kotlinx.coroutines#avoiding-including-the-debug-infrastructure-in-the-resulting-apk
            excludes.add("DebugProbesKt.bin")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }

    sourceSets.getByName("main") {
        java.setSrcDirs(listOf("src/main/kotlin"))
        java.setSrcDirs(listOf("src/withoutCrashLogging/kotlin", "src/withGoogleServices/kotlin"))
    }
    sourceSets.getByName("test") {
        java.setSrcDirs(listOf("src/test/kotlin"))
    }

    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
}

composeCompiler {
    stabilityConfigurationFile = rootProject.layout.projectDirectory.file("stability_config.conf")
}

unMock {
    keep("android.net.Uri")
    keep("android.os.Bundle")
    keepStartingWith("org.")
    keepStartingWith("libcore.")
    keepStartingWith("android.content.Intent")
    keepAndRename("java.nio.charset.Charsets").to("xjava.nio.charset.Charsets")
}

dependencies {
    coreLibraryDesugaring(libs.desugar)
    implementation(libs.kotlin.stdlib)

    /* Dependency Injection */
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    /* Android & Kotlin extensions */
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.jdk9)
    implementation(libs.androidx.core)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.splashscreen)

    /* Database */
    implementation(libs.realm) // doomed, to be phased out 💔
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    /* Compose */
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.materialIconsExtended)
    implementation(libs.androidx.compose.uiToolingPreview)
    debugImplementation(libs.androidx.compose.uiTooling)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    /* Permissions */
    implementation(libs.peko)

    /* Scheduling */
    implementation(libs.androidx.work.runtime)

    /* JSON serialization & deserialization */
    implementation(libs.gson)

    /* Reading & writing zip files for Import & Export */
    implementation(libs.zip4j)

    /* Testing */
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.mockk)
    testImplementation(libs.androidx.test)
    testImplementation(libs.kotlinx.coroutines.test)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}
