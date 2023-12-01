import com.android.build.gradle.internal.tasks.factory.dependsOn
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.internal.api.BaseVariantOutputImpl

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("com.bugsnag.android.gradle")
    id("io.realm.kotlin")
    id("de.mobilej.unmock")
    id("com.google.dagger.hilt.android")
}

val coroutinesVersion: String by properties
val gsonVersion: String by properties
val okHttpVersion: String by properties
val kotlinTestJunit5Version: String by properties
val mockkVersion: String by properties
val androidCoreKtxTestVersion: String by properties
val hiltVersion: String by properties

val bugsnagAPIKey: String by rootProject.ext
val autoBuildDocs: Boolean by rootProject.ext
val useBugsnag = bugsnagAPIKey.isNotEmpty()

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

    compileSdk = 34

    kotlinOptions {
        languageVersion = "1.6"
        jvmTarget = "1.8"
    }

    lint {
        disable.add("MissingTranslation")
        disable.add("Instantiatable")
    }

    defaultConfig {
        applicationId = "ch.rmy.android.http_shortcuts"
        minSdk = 21
        targetSdk = 33

        // Version name and code must remain as literals so that F-Droid can read them
        versionName = "3.9.0"
        // 11,(2 digits major),(2 digits minor),(2 digits patch),(2 digits build)
        versionCode = 1103090000

        buildConfigField("String", "BUGSNAG_API_KEY", "\"$bugsnagAPIKey\"")
        buildConfigField("String", "BUILD_TIMESTAMP", "\"${rootProject.ext["buildTimestamp"]}\"")

        manifestPlaceholders["bugsnagAPIKey"] = bugsnagAPIKey
        testInstrumentationRunnerArguments["package"] = "ch.rmy.android.http_shortcuts"
        vectorDrawables.useSupportLibrary = true

        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
        }

        resourceConfigurations.addAll(
            setOf(
                "en",
                "en-rGB",
                "ca",
                "de",
                "de-rCH",
                "es",
                "es-rMX",
                "fr",
                "in",
                "it",
                "hu",
                "nb",
                "pl",
                "pt-rBR",
                "ru",
                "tr",
                "zh-rCN",
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
        )
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
            ndk.debugSymbolLevel = "SYMBOL_TABLE"

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
        compose = true
        buildConfig = true
        viewBinding = false
        dataBinding = false
        aidl = false
        renderScript = false
        resValues = false
        shaders = false
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        isCoreLibraryDesugaringEnabled = true
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
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    implementation(project(path = ":framework"))

    /* Dependency Injection */
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    kapt("com.google.dagger:hilt-android-compiler:$hiltVersion")
    implementation("androidx.hilt:hilt-work:1.1.0")
    kapt("androidx.hilt:hilt-compiler:1.1.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    /* Support libraries */
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.core:core-splashscreen:1.0.1")

    /* Color picker */
    implementation("com.github.skydoves:colorpickerview:2.3.0")

    /* Compose */
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.material3:material3:1.2.0-alpha09")
    implementation("androidx.navigation:navigation-compose:2.7.5")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")
    implementation("org.burnoutcrew.composereorderable:reorderable:0.9.6")
    implementation("com.github.qawaz:compose-code-editor:2.0.3")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.30.1")

    /* Image cropping */
    implementation("com.github.yalantis:ucrop:2.2.8")

    /* Image display */
    implementation("io.coil-kt:coil-compose:2.5.0")

    /* Image meta data extraction */
    implementation("androidx.exifinterface:exifinterface:1.3.6")

    /* HTTP & Network */
    implementation("com.squareup.okhttp3:okhttp:$okHttpVersion")
    implementation("org.brotli:dec:0.1.2")
    implementation("io.github.rburgst:okhttp-digest:3.1.0")
    implementation("com.github.franmontiel:PersistentCookieJar:v1.0.1")
    implementation("org.conscrypt:conscrypt-android:2.5.2")

    /* Custom Tabs (for Browser Shortcuts) */
    implementation("androidx.browser:browser:1.7.0")

    /* Permissions */
    implementation("com.markodevcic:peko:2.2.0")

    /* Scheduling */
    implementation("androidx.work:work-runtime-ktx:2.8.1")

    /* Tasker integration */
    implementation("com.joaomgcd:taskerpluginlibrary:0.4.10")

    /* Scripting */
    // This is not the latest version, but it's the latest one that has a published artifact, and the newer ones lead to
    // a larger build size without adding significant benefits, so I'm keeping this at the old version for now
    implementation("com.github.LiquidPlayer:LiquidCore:0.6.2")

    /* Location lookup (for Scripting) */
    debugImplementation("com.google.android.gms:play-services-location:21.0.1")
    "releaseFullImplementation"("com.google.android.gms:play-services-location:21.0.1")

    /* Biometric confirmation */
    implementation("androidx.biometric:biometric:1.2.0-alpha05")

    /* MQTT (for Scripting) */
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")

    /* Password hashing */
    implementation("org.mindrot:jbcrypt:0.4")

    /* Crash Reporting */
    "releaseFullImplementation"("com.bugsnag:bugsnag-android:5.31.3")

    /* cURL import & export */
    implementation(project(path = ":curl_command"))

    /* Favicon fetching */
    implementation(project(path = ":favicon_grabber"))

    /* JSON serialization & deserialization */
    implementation("com.google.code.gson:gson:$gsonVersion")

    /* Testing */
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:$kotlinTestJunit5Version")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("androidx.test:core-ktx:$androidCoreKtxTestVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
}

kapt {
    correctErrorTypes = true
}

fun generateHtmlFromMarkdown(inputFile: String, outputFile: String, templateFile: String, mutate: String.() -> String = { this }) {
    val changelogMarkdown = File("../$inputFile").readText()
    val template = File(templateFile).readText()
    val flavour = GFMFlavourDescriptor()
    val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(changelogMarkdown)
    val html = HtmlGenerator(changelogMarkdown, parsedTree, flavour)
        .generateHtml()
        .removePrefix("<body>")
        .removeSuffix("</body>")
        .mutate()
    File("app/src/main/assets/$outputFile").writeText(
        template.replace("<!-- CONTENT -->", html)
    )
}

tasks.register("syncChangeLog") {
    description = "copies the CHANGELOG.md file's content into the app so it can be displayed"

    doFirst {
        generateHtmlFromMarkdown(
            inputFile = "CHANGELOG.md",
            outputFile = "changelog.html",
            templateFile = "changelog_template.html",
        )
    }
}

tasks.register("syncDocumentation") {
    description = "copies the documentation markdown files' contents into the app so they can be displayed"

    val files = listOf(
        "advanced",
        "categories",
        "documentation",
        "execution-flow",
        "faq",
        "import-export",
        "introduction",
        "permissions",
        "privacy-policy",
        "scripting",
        "shortcuts",
        "variables",
    )

    doFirst {
        files.forEach { fileName ->
            generateHtmlFromMarkdown(
                inputFile = "docs/$fileName.md",
                outputFile = "docs/$fileName.html",
                templateFile = "documentation_template.html",
            ) {
                replace("src=\"../assets/documentation/", "src=\"file:///android_asset/docs/assets/")
            }
        }
    }
}
