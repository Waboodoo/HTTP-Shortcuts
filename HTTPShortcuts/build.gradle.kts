buildscript {
    repositories {
        jcenter()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.0.1")
        classpath("io.realm:realm-gradle-plugin:7.0.2")
        classpath("com.bugsnag:bugsnag-android-gradle-plugin:4.+")
        classpath("org.jetbrains:markdown:0.1.45")
        classpath(kotlin("gradle-plugin", "1.4.0"))
    }
}

ext {
    set("bugsnagAPIKey", System.getenv("BUGSNAG_API_KEY") ?: "")
    set("buildTimestamp", java.util.Date().time.toString())

    // Encryption keys only used for legacy migrations, will hopefully be removed soon
    set("realmEncryptionKey", if (System.getenv("REALM_ENCRYPTION_KEY") != null) {
        "Wr3DmyNj0[{(,8g%jX2{03P45_p`N6|2zX08poC7a96dL9,FR_9|Uw<2%]?3Ij)4"
    } else {
        "ZX06poC7a96dL9,FR_9|Ww<2%]?4Ij(3wR3DmyNj0[{(,8g%jX2{03P45_p`N6|2"
    })
}

allprojects {
    repositories {
        jcenter()
        maven("https://jitpack.io")
    }
}
repositories {
    mavenCentral()
}

// Task to copy the changelog from the CHANGELOG.md file into the app so it can be displayed
task("syncChangeLog") {
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
