import com.android.utils.forEach
import java.awt.Color
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import org.w3c.dom.Element

plugins {
    id("com.android.application")
}

android {
    namespace = "ch.rmy.android.http_shortcuts.shelltemplate"

    compileSdk = 37

    defaultConfig {
        applicationId = "ch.rmy.android.http_shortcuts.shelltemplate"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
}

val generatedIconResDir = layout.buildDirectory.dir("generated/res/shell-template-icon").get().asFile
val generatedStringResDir = layout.buildDirectory.dir("generated/res/shell-template-strings").get().asFile
val generatedIconFiles = mapOf(
    "mipmap-mdpi" to 48,
    "mipmap-hdpi" to 72,
    "mipmap-xhdpi" to 96,
    "mipmap-xxhdpi" to 144,
    "mipmap-xxxhdpi" to 192,
)
    .map { (density, size) ->
        generatedIconResDir.resolve("$density/ic_launcher_shell.png") to size
    }

val generateShellTemplateIcon by tasks.registering {
    generatedIconFiles.forEach { (file, _) ->
        outputs.file(file)
    }

    doLast {
        generatedIconFiles.forEach { (file, size) ->
            file.parentFile.mkdirs()
            val image = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
            val graphics = image.createGraphics()
            try {
                graphics.color = Color(0x2D, 0x6C, 0xDF)
                graphics.fillRect(0, 0, size, size)
                graphics.color = Color.WHITE
                graphics.fillRect((size * 0.31f).toInt(), (size * 0.33f).toInt(), (size * 0.38f).toInt(), (size * 0.08f).toInt())
                graphics.fillRect((size * 0.31f).toInt(), (size * 0.46f).toInt(), (size * 0.38f).toInt(), (size * 0.08f).toInt())
                graphics.fillRect((size * 0.31f).toInt(), (size * 0.58f).toInt(), (size * 0.25f).toInt(), (size * 0.08f).toInt())
            } finally {
                graphics.dispose()
            }
            ImageIO.write(image, "png", file)
        }
    }
}

val shellTemplateStringNames = setOf(
    "message_shell_apk_shortcut_not_found",
)

// Keep shell APK text in the main app's translation files, but copy only the strings the tiny template needs.
// This lets translators use the existing localization workflow without bloating every generated shell APK.
val syncShellTemplateStrings by tasks.registering {
    val appResDir = project(":app").layout.projectDirectory.dir("src/main/res").asFile
    val appStringFiles = fileTree(appResDir) {
        include("values*/strings.xml")
    }

    inputs.files(appStringFiles)
    outputs.dir(generatedStringResDir)

    doLast {
        generatedStringResDir.deleteRecursively()

        val documentBuilderFactory = DocumentBuilderFactory.newInstance().apply {
            setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
        }
        val documentBuilder = documentBuilderFactory.newDocumentBuilder()
        val transformer = TransformerFactory.newInstance()
            .newTransformer()
            .apply {
                setOutputProperty(OutputKeys.ENCODING, "utf-8")
                setOutputProperty(OutputKeys.INDENT, "yes")
            }

        appStringFiles.files.forEach { stringsFile ->
            val inputDocument = documentBuilder.parse(stringsFile)
            val outputDocument = documentBuilder.newDocument()
            val outputResources = outputDocument.createElement("resources")
            outputDocument.appendChild(outputResources)

            val stringElements = inputDocument.documentElement.getElementsByTagName("string")
            stringElements.forEach { item ->
                val element = item as Element
                if (element.getAttribute("name") in shellTemplateStringNames) {
                    outputResources.appendChild(outputDocument.importNode(element, true))
                }
            }

            if (outputResources.childNodes.length > 0) {
                val outputFile = generatedStringResDir.resolve("${stringsFile.parentFile.name}/strings.xml")
                outputFile.parentFile.mkdirs()
                transformer.transform(DOMSource(outputDocument), StreamResult(outputFile))
            }
        }
    }
}

android.sourceSets.getByName("main").res.srcDir(generatedIconResDir)
android.sourceSets.getByName("main").res.srcDir(generatedStringResDir)

tasks.named("preBuild") {
    dependsOn(generateShellTemplateIcon)
    dependsOn(syncShellTemplateStrings)
}
