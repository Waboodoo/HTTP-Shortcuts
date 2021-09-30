buildscript {
    repositories {
        mavenCentral()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.2.2")
        classpath("io.realm:realm-gradle-plugin:10.8.0")
        classpath("com.bugsnag:bugsnag-android-gradle-plugin:5.8.1")
        classpath("org.jetbrains:markdown:0.1.45")
        classpath(kotlin("gradle-plugin", "1.5.20"))
    }
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
        jcenter()
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
        val html = org.intellij.markdown.html.HtmlGenerator(changelogMarkdown, parsedTree, flavour).generateHtml()
        File("app/src/main/assets/changelog.html").writeText(
            template.replace("<!-- CONTENT -->", html)
        )
    }
}

