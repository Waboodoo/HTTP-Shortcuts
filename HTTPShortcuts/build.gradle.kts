buildscript {
    repositories {
        mavenCentral()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.2.1")
        classpath("io.realm:realm-gradle-plugin:10.8.1")
        classpath("com.bugsnag:bugsnag-android-gradle-plugin:7.2.1")
        classpath("org.jetbrains:markdown:0.3.1")
        classpath(kotlin("gradle-plugin", "1.7.0"))
    }
}

plugins {
    id("com.diffplug.spotless") version "6.4.2"
    id("org.jetbrains.kotlin.jvm") version "1.7.0" apply false
}

ext {
    set("bugsnagAPIKey", System.getenv("BUGSNAG_API_KEY") ?: "")
    set("poeditorAPIKey", System.getenv("PO_EDITOR_API_KEY") ?: "")
    set("poeditorProjectId", System.getenv("PO_EDITOR_PROJECT_ID") ?: "")
    set("buildTimestamp", java.util.Date().time.toString())
}

allprojects {
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        google()
    }

    apply {
        plugin("com.diffplug.spotless")
    }

    spotless {
        kotlin {
            target(fileTree(".") {
                include("**/*.kt")
            })
            ktlint("0.43.2").userData(mapOf(
                "max_line_length" to "150",
                "indent_size" to "4",
                "insert_final_newline" to "true",
            ))
        }
    }
}

tasks.register("syncChangeLog") {
    description = "copies the CHANGELOG.md file's content into the app so it can be displayed"

    doLast {
        val changelogMarkdown = File("../CHANGELOG.md").readText()
        val template = File("changelog_template.html").readText()
        val flavour = org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor()
        val parsedTree = org.intellij.markdown.parser.MarkdownParser(flavour)
            .buildMarkdownTreeFromString(changelogMarkdown)
        val html = org.intellij.markdown.html.HtmlGenerator(changelogMarkdown, parsedTree, flavour)
            .generateHtml()
            .removePrefix("<body>")
            .removeSuffix("</body>")
        File("app/src/main/assets/changelog.html").writeText(
            template.replace("<!-- CONTENT -->", html)
        )
    }
}
