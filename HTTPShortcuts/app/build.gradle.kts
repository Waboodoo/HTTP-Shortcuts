import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.opencsv.CSVReader
import `in`.wilsonl.minifyhtml.Configuration
import `in`.wilsonl.minifyhtml.MinifyHtml
import java.io.FileReader
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser

plugins {
    id("com.android.application")
    id("kotlin-android")
    alias(libs.plugins.ksp)
    id("com.bugsnag.android.gradle")
    id("de.mobilej.unmock")
    id("com.google.dagger.hilt.android")
    alias(libs.plugins.compose.compiler)
}

val bugsnagAPIKey = System.getenv("HTTP_SHORTCUTS_BUGSNAG_API_KEY") ?: ""
val autoBuildDocs = System.getenv("HTTP_SHORTCUTS_AUTO_BUILD_DOCS").toBoolean()
val useBugsnag = bugsnagAPIKey.isNotEmpty()
val buildTimestamp = System.currentTimeMillis()

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

    compileSdk = 36

    kotlin {
        jvmToolchain(11)
    }

    lint {
        disable.add("MissingTranslation")
        disable.add("Instantiatable")
    }

    defaultConfig {
        applicationId = "ch.rmy.android.http_shortcuts"
        minSdk = 26
        targetSdk = 35

        // Version name and code must remain as literals so that F-Droid can read them
        versionName = "4.0.0"
        // 11,(2 digits major),(2 digits minor),(2 digits patch),(2 digits build)
        versionCode = 1104000001

        buildConfigField("String", "BUGSNAG_API_KEY", "\"$bugsnagAPIKey\"")
        buildConfigField("long", "BUILD_TIMESTAMP", buildTimestamp.toString())

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
    implementation(libs.kotlin.stdlib)

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
    implementation(libs.androidx.compose.materialIconsExtended)
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

    /* MQTT (for Scripting) */
    implementation(libs.paho.mqtt)

    /* Password hashing */
    implementation(libs.jbcrypt)

    /* Crash Reporting */
    "releaseFullImplementation"(libs.bugsnag.android)

    /* cURL import & export */
    implementation(project(path = ":curl_command"))

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

fun generateHtmlFromMarkdown(
    inputFile: String,
    outputFile: String,
    templateFile: String,
    processMarkdown: String.() -> String = { this },
    processHtml: String.() -> String = { this },
) {
    val changelogMarkdown = File("../$inputFile").readText()
        .processMarkdown()
    val template = File(templateFile).readText()
    val flavour = GFMFlavourDescriptor()
    val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(changelogMarkdown)
    val html = HtmlGenerator(changelogMarkdown, parsedTree, flavour)
        .generateHtml()
        .removePrefix("<body>")
        .removeSuffix("</body>")
        .processHtml()
        .let { html ->
            val config = Configuration.Builder()
                .setRemoveBangs(true)
                .setKeepHtmlAndHeadOpeningTags(true)
                .setMinifyCss(true)
                .build()
            MinifyHtml.minify(html, config)
                .replace("&LT", "&lt;")
        }
    File("app/src/main/assets/$outputFile").writeText(
        template.replace("<!-- CONTENT -->", html)
            .replace("<!-- .* -->\\s*".toRegex(), ""),
    )
}

fun processStoreListings() {
    val root = File("..")
    val sourceDir = File(root, "store_listings")
    val translatedFiles = sourceDir
        .listFiles { file: File -> file.isDirectory }!!
        .map { File(it, "store_listing.csv") }
        .filter { it.exists() }
        .map { it.parentFile.name to it }
        .plus("en-US" to File(sourceDir, "store_listing.csv"))
        .forEach { (language, csvFile) ->
            val targetDir = File(root, "metadata/$language")
            CSVReader(FileReader(csvFile))
                .filter { (_, value) -> value.isNotEmpty() }
                .forEach { (key, value) ->
                    when (key) {
                        "short_description" -> {
                            targetDir.mkdirs()
                            val targetFile = File(targetDir, "short_description.txt")
                            targetFile.createNewFile()
                            targetFile.writeText(value)
                        }
                        "full_description" -> {
                            targetDir.mkdirs()
                            val targetFile = File(targetDir, "full_description.txt")
                            targetFile.createNewFile()
                            targetFile.writer().use { writer ->
                                var firstLine = true
                                var listDepth = 0
                                value.lines()
                                    .plus("END")
                                    .forEach { line ->
                                        val newDepth = when {
                                            line.startsWith("- ") -> 1
                                            line.startsWith("  - ") -> 2
                                            else -> 0
                                        }
                                        val line = when {
                                            line.startsWith("- ") -> line.removePrefix("- ")
                                            line.startsWith("  - ") -> line.removePrefix("  - ")
                                            else -> line
                                        }

                                        if (listDepth < newDepth) {
                                            for (i in listDepth until newDepth) {
                                                writer.append("<ul>")
                                                writer.append("<li>")
                                            }
                                        } else if (listDepth > newDepth) {
                                            for (i in newDepth until listDepth) {
                                                writer.append("</li>")
                                                writer.append("</ul>")
                                            }
                                            if (newDepth != 0) {
                                                writer.append("</li>")
                                                writer.append("<li>")
                                            }
                                        } else if (newDepth != 0) {
                                            writer.append("</li>")
                                            writer.append("<li>")
                                        } else if (!firstLine) {
                                            writer.appendLine()
                                        }

                                        listDepth = newDepth

                                        if (line != "END") {
                                            writer.append(line)
                                        }

                                        firstLine = false
                                    }
                            }
                        }
                    }
                }
        }

    println(translatedFiles)
}

tasks.register("syncChangeLog") {
    description = "copies the CHANGELOG.md file's content into the app so it can be displayed"
    val maxSections = 10

    doFirst {
        generateHtmlFromMarkdown(
            inputFile = "CHANGELOG.md",
            outputFile = "changelog.html",
            templateFile = "changelog_template.html",
            processMarkdown = {
                var sections = 0
                lineSequence()
                    .takeWhile { line ->
                        if (line.startsWith("## ")) {
                            sections++
                        }
                        sections <= maxSections
                    }
                    .joinToString(separator = "\n")
                    .plus("\nFor older versions, check the [full changelog](https://github.com/Waboodoo/HTTP-Shortcuts/blob/develop/CHANGELOG.md).")
            },
        )
    }
}

tasks.register("syncDocumentation") {
    description = "copies the documentation markdown files' contents into the app so they can be displayed"

    val files = listOf(
        "advanced",
        "categories",
        "directories",
        "documentation",
        "execution-flow",
        "faq",
        "import-export",
        "introduction",
        "permissions",
        "privacy-policy",
        "scripting",
        "scripting-examples",
        "shortcuts",
        "variables",
    )

    doFirst {
        files.forEach { fileName ->
            generateHtmlFromMarkdown(
                inputFile = "docs/$fileName.md",
                outputFile = "docs/$fileName.html",
                templateFile = "documentation_template.html",
                processHtml = {
                    replace("src=\"../assets/documentation/", "src=\"file:///android_asset/docs/assets/")
                },
            )
        }
    }
}

tasks.register("syncStoreListings") {
    description = "processes the store listing CSV files to generate the metadata files for F-Droid"
    doFirst {
        processStoreListings()
    }
}
