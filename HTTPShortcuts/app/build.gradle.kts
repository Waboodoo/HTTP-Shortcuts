import buildSrc.LocalProperties
import buildSrc.processStoreListings
import buildSrc.syncChangeLog
import buildSrc.syncDocumentation
import buildSrc.syncIconKeywords
import buildSrc.syncTranslationProgress
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import com.android.build.gradle.internal.tasks.factory.dependsOn

plugins {
    id("com.android.application")
    id("kotlin-android")
    alias(libs.plugins.ksp)
    id("com.bugsnag.android.gradle")
    id("de.mobilej.unmock")
    id("com.google.dagger.hilt.android")
    alias(libs.plugins.compose.compiler)
}

val bugsnagAPIKey = LocalProperties.getString("bugsnag_api_key") ?: ""
val autoBuildDocs = LocalProperties.getBoolean("autobuild_docs") ?: false
val useBugsnag = bugsnagAPIKey.isNotEmpty()
val buildDate = System.currentTimeMillis() / (24 * 60 * 60 * 1000L)

class OutputFileNameVariantAction : Action<ApplicationVariant> {
    override fun execute(variant: ApplicationVariant) {
        variant.outputs.all(VariantOutputAction())
    }

    class VariantOutputAction : Action<BaseVariantOutput> {
        override fun execute(output: BaseVariantOutput) {
            if (output is BaseVariantOutputImpl) {
                output.outputFileName = output.outputFileName.replace("-releaseFull.apk", "-release.apk")
            }
        }
    }
}

android {
    namespace = "ch.rmy.android.http_shortcuts"

    compileSdk = 37

    kotlin {
        jvmToolchain(17)
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }

    lint {
        disable.add("MissingTranslation")
        disable.add("Instantiatable")
    }

    defaultConfig {
        applicationId = "ch.rmy.android.http_shortcuts"
        minSdk = 26
        targetSdk = 36

        // Version name and code must remain as literals so that F-Droid can read them
        versionName = "4.7.0"
        // 11,(2 digits major),(2 digits minor),(2 digits patch),(2 digits build)
        versionCode = 1104070000

        buildConfigField("String", "BUGSNAG_API_KEY", "\"$bugsnagAPIKey\"")
        buildConfigField("int", "BUILD_DATE", buildDate.toString())

        manifestPlaceholders["bugsnagAPIKey"] = bugsnagAPIKey
        testInstrumentationRunnerArguments["package"] = "ch.rmy.android.http_shortcuts"
        vectorDrawables.useSupportLibrary = true

        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
        }
    }

    androidResources {
        localeFilters += setOf(
            "en",
            "en-rGB",
            "bg",
            "ca",
            "de",
            "de-rCH",
            "es",
            "es-rMX",
            "fr",
            "in",
            "it",
            "hu",
            "pl",
            "pt-rBR",
            "ru",
            "tr",
            "zh-rCN",
            "zh-rTW",
            "ja",
            "ko",
            "iw",
            "ar",
            "cs",
            "el",
            "fa",
            "nl",
            "pt",
            "vi",
        )

        ignoreAssetsPatterns += setOf(
            "!*.js",
            "!*.mjs",
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
            applicationIdSuffix = ".debug"
            signingConfig = signingConfigs["development"]

            buildConfigField("String", "BUILD_TYPE", "\"DEBUG\"")
        }

        /* Used for F-Droid */
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

            buildConfigField("String", "BUILD_TYPE", "\"RELEASE\"")
        }

        /* Used for Play Store & GitHub release page */
        create("releaseFull") {
            isMinifyEnabled = true
            isShrinkResources = true
            ndk.debugSymbolLevel = "SYMBOL_TABLE"

            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

            buildConfigField("String", "BUILD_TYPE", "\"RELEASE_FULL\"")
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
        resValues = false
        shaders = false
    }

    packaging {
        jniLibs {
            excludes.add("META-INF/*")
        }
        resources {
            excludes.add("META-INF/*")

            // See https://github.com/Kotlin/kotlinx.coroutines#avoiding-including-the-debug-infrastructure-in-the-resulting-apk
            excludes.add("DebugProbesKt.bin")
        }
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

    if (autoBuildDocs) {
        project.tasks.preBuild.dependsOn("syncDocumentation")
        project.tasks.preBuild.dependsOn("syncChangeLog")
    }

    applicationVariants.all(OutputFileNameVariantAction())

    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
}

composeCompiler {
    includeSourceInformation = true
    stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("stability_config.conf"))
}

unMock {
    keep("android.net.Uri")
    keep("android.os.Bundle")
    keepStartingWith("org.")
    keepStartingWith("libcore.")
    keepStartingWith("android.content.Intent")
    keepAndRename("java.nio.charset.Charsets").to("xjava.nio.charset.Charsets")
}

bugsnag {
    enabled.set(useBugsnag)
    uploadJvmMappings.set(useBugsnag)
    uploadNdkMappings.set(false)
    uploadNdkUnityLibraryMappings.set(false)
    reportBuilds.set(useBugsnag)
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(project(path = ":framework"))

    /* Dependency Injection */
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.androidx.hilt.work)
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
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    /* Color picker */
    implementation(libs.colorpickerview)

    /* Compose */
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.uiToolingPreview)
    debugImplementation(libs.androidx.compose.uiTooling)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.reorderable)
    implementation(libs.composeCodeEditor)
    implementation(libs.composableTable)

    /* Image cropping */
    implementation(libs.androidImageCropper)

    /* Image display */
    implementation(libs.coil.compose)
    implementation(libs.zoomable)

    /* Image meta data extraction */
    implementation(libs.androidx.exifinterface)

    /* HTTP & Network */
    implementation(libs.okhttp3)
    implementation(libs.brotli)
    implementation(libs.okhttpDigest)
    implementation(libs.persistentCookieJar)
    implementation(libs.conscrypt)

    /* Custom Tabs (for Browser Shortcuts) */
    implementation(libs.androidx.browser)

    /* HTML parsing */
    implementation(libs.jsoup)

    /* Permissions */
    implementation(libs.peko)

    /* Scheduling */
    implementation(libs.androidx.work.runtime)

    /* Tasker integration */
    implementation(libs.taskerplugin)

    /* Scripting */
    implementation(project(path = ":scripting"))

    /* Location lookup (for Scripting) */
    debugImplementation(libs.playServices.location)
    "releaseFullImplementation"(libs.playServices.location)

    /* Biometric confirmation */
    implementation(libs.androidx.biometric)

    /* MQTT */
    implementation(libs.paho.mqtt)

    /* Password hashing (for password lock) */
    implementation(libs.jbcrypt)

    /* Crash Reporting */
    "releaseFullImplementation"(libs.bugsnag.android)

    /* cURL import & export */
    implementation(project(path = ":curl_command"))

    /* Material Design icon fetching */
    implementation(project(path = ":icon_fetcher"))
    implementation(libs.coil.network.okhttp)
    implementation(libs.coil.svg)

    /* Favicon fetching */
    implementation(project(path = ":favicon_grabber"))

    /* JSON serialization & deserialization */
    implementation(libs.gson)

    /* Reading & writing zip files for Import & Export */
    implementation(libs.zip4j)

    /* Google Assistant integration */
    "releaseFullImplementation"(libs.androidx.googleShortcuts)

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

tasks.register("syncChangeLog") {
    description = "copies the CHANGELOG.md file's content into the app so it can be displayed"
    doFirst {
        syncChangeLog()
    }
}

tasks.register("syncDocumentation") {
    description = "copies the documentation markdown files' contents into the app so they can be displayed"
    doFirst {
        syncDocumentation()
    }
}

tasks.register("syncIconsKeywords") {
    description = "copies and compresses the icon index file into the app"
    doFirst {
        syncIconKeywords()
    }
}

tasks.register("syncStoreListings") {
    description = "processes the store listing CSV files to generate the metadata files for F-Droid"
    doFirst {
        processStoreListings()
    }
}

tasks.register("syncTranslationProgress") {
    description = "fetches translation progress and stores it"
    doFirst {
        syncTranslationProgress()
    }
}
