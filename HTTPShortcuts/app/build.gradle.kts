import com.android.build.gradle.internal.tasks.factory.dependsOn
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.internal.api.BaseVariantOutputImpl

plugins {
    id("de.jansauer.poeditor") version "1.1.0"
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("com.bugsnag.android.gradle")
    id("realm-android")
}

val hamcrestVersion: String by properties
val junitVersion: String by properties
val robolectricVersion: String by properties
val mockitoVersion: String by properties
val coroutinesVersion: String by properties

val bugsnagAPIKey: String by rootProject.ext
val poeditorAPIKey: String by rootProject.ext
val poeditorProjectId: String by rootProject.ext
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

    compileSdk = 33

    kotlinOptions {
        languageVersion = "1.6"
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
        versionName = "2.30.0"
        // 11,(2 digits major),(2 digits minor),(2 digits patch),(2 digits build)
        versionCode = 1102300001

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

    if (autoBuildDocs) {
        project.tasks.preBuild.dependsOn("syncDocumentation")
        project.tasks.preBuild.dependsOn("syncChangeLog")
    }

    applicationVariants.all(OutputFileNameVariantAction())
}

bugsnag {
    enabled.set(useBugsnag)
    uploadJvmMappings.set(useBugsnag)
    uploadNdkMappings.set(false)
    uploadNdkUnityLibraryMappings.set(false)
    reportBuilds.set(useBugsnag)
}

dependencies {
    implementation(project(path = ":framework"))

    /* Dependency Injection */
    kapt("com.google.dagger:dagger-compiler:2.41")

    /* Support libraries */
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    implementation("androidx.core:core-splashscreen:1.0.0")

    /* Dialogs / Activities */
    implementation("com.afollestad.material-dialogs:core:3.3.0")
    implementation("com.afollestad.material-dialogs:input:3.3.0")
    implementation("com.github.skydoves:colorpickerview:2.2.4")

    /* Image cropping */
    implementation("com.github.yalantis:ucrop:2.2.8")

    /* Image display */
    implementation("com.squareup.picasso:picasso:2.8")

    /* Image meta data extraction */
    implementation("androidx.exifinterface:exifinterface:1.3.6")

    /* HTTP & Network */
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("io.github.rburgst:okhttp-digest:2.6")
    implementation("com.github.franmontiel:PersistentCookieJar:v1.0.1")
    implementation("org.conscrypt:conscrypt-android:2.5.2")

    /* Permissions */
    implementation("com.markodevcic:peko:2.2.0")

    /* Scheduling */
    implementation("androidx.work:work-runtime-ktx:2.8.0")

    /* Tasker integration */
    implementation("com.joaomgcd:taskerpluginlibrary:0.4.7")

    /* Scripting */
    // This is not the latest version, but it's the latest one that has a published artifact, and the newer ones lead to
    // a larger build size without adding significant benefits, so I'm keeping this at the old version for now
    implementation("com.github.LiquidPlayer:LiquidCore:0.6.2")

    /* Location lookup (for Scripting) */
    debugImplementation("com.google.android.gms:play-services-location:21.0.1")
    "releaseFullImplementation"("com.google.android.gms:play-services-location:21.0.1")

    /* MQTT (for Scripting) */
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")

    /* Password hashing */
    implementation("org.mindrot:jbcrypt:0.4")

    /* Crash Reporting */
    "releaseFullImplementation"("com.bugsnag:bugsnag-android:5.23.1")

    /* cURL import & export */
    implementation(project(path = ":curl_command"))

    /* Favicon fetching */
    implementation(project(path = ":favicon_grabber"))

    /* JSON serialization & deserialization */
    implementation("com.google.code.gson:gson:2.10.1")

    /* Testing */
    testImplementation("org.hamcrest:hamcrest-library:$hamcrestVersion")
    testImplementation("junit:junit:$junitVersion")
    testImplementation("org.robolectric:robolectric:$robolectricVersion")
    testImplementation("org.mockito:mockito-core:$mockitoVersion")
    testImplementation("org.mockito:mockito-inline:$mockitoVersion")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    testImplementation("androidx.test:core-ktx:1.5.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
}

configurations.all {
    // Exclude RxJava2 (which would otherwise be included by Realm) as we don't need it
    exclude(group = "io.reactivex.rxjava2", module = "rxandroid")
    exclude(group = "io.reactivex.rxjava2", module = "rxjava")
}

poeditor {
    apiKey.set(poeditorAPIKey)
    projectId.set(poeditorProjectId)

    terms(
        mapOf(
            "lang" to "en",
            "file" to "src/main/res/values/strings.xml",
            "updating" to "terms_translations",
            "overwrite" to true,
            "sync_terms" to true,
        )
    )
    // translation definitions omitted as the plugin currently does not support filtering by "translated" status, making its pull feature unusable
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
                replace("src=\"../", "src=\"https://http-shortcuts.rmy.ch/")
            }
        }
    }
}
