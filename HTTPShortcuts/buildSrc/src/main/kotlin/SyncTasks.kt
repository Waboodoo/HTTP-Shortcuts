package buildSrc

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.opencsv.CSVReader
import `in`.wilsonl.minifyhtml.Configuration
import `in`.wilsonl.minifyhtml.MinifyHtml
import okhttp3.OkHttpClient
import okhttp3.Request
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import java.io.File
import java.io.FileReader
import java.util.zip.GZIPOutputStream

fun syncChangeLog() {
    val maxSections = 10
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

fun syncDocumentation() {
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

private fun generateHtmlFromMarkdown(
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

fun syncIconKeywords() {
    val processedFileContents = File("../assets/icons_keywords.txt").readLines()
        .filter { line ->
            !line.isEmpty() && !line.startsWith("#")
        }
        .joinToString("\n")
    GZIPOutputStream(File("app/src/main/assets/icons_keywords").outputStream()).use { outputStream ->
        processedFileContents.byteInputStream().use { inputStream ->
            inputStream.copyTo(outputStream)
        }
    }
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

fun syncTranslationProgress() {
    val config = File("../crowdin.yml").readLines()
    val apiToken = config.first { it.startsWith("\"api_token\"") }.split(": ")[1].trim('"')
    val projectId = config.first { it.startsWith("\"project_id\"") }.split(": ")[1].trim('"')

    val outputFile = File("app/src/main/assets/translation-progress.txt")
    val writer = outputFile.printWriter()

    val jsonReport = OkHttpClient.Builder()
        .build()
        .newCall(
            Request.Builder()
                .get()
                .url("https://api.crowdin.com/api/v2/projects/$projectId/languages/progress?limit=100")
                .header("Authorization", "Bearer $apiToken")
                .build(),
        )
        .execute()
        .body
        .string()
    val report = Gson().fromJson(jsonReport, JsonObject::class.java)
    report.get("data").asJsonArray.forEach { languageReportElement ->
        val languageReport = languageReportElement.asJsonObject.getAsJsonObject("data")
        val rawLanguage = languageReport.getAsJsonPrimitive("languageId").asString
        val progress = languageReport.getAsJsonPrimitive("translationProgress").asInt
        if (progress > 10) {
            val language = if (rawLanguage == "he") {
                "iw"
            } else if (rawLanguage == "id") {
                "in"
            } else if ('-' in rawLanguage) {
                val parts = rawLanguage.split('-')
                if (parts[0].equals(parts[1], ignoreCase = true)) {
                    parts[0]
                } else {
                    "${parts[0]}-r${parts[1]}"
                }
            } else {
                rawLanguage
            }
            writer.println("$language:$progress")
        }
    }
    writer.println("en:100")
    writer.flush()
    writer.close()
}
