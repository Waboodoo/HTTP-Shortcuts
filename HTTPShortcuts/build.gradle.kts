val kotlinVersion: String by project

buildscript {
    repositories {
        jcenter()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.4.1")
        classpath("io.realm:realm-gradle-plugin:5.12.0")
        classpath(kotlin("gradle-plugin", "1.3.41"))
    }
}

val versionMajor: String by project
val versionMinor: String by project
val versionPatch: String by project
val versionBuild: String by project

ext {
    set("versionName", "$versionMajor.$versionMinor.$versionPatch")
    set("versionCode", ((((versionMajor.toInt()) * 1000 + versionMinor.toInt()) * 1000 + versionPatch.toInt()) * 1000) + versionBuild.toInt())
    set("bugsnagAPIKey", System.getenv("BUGSNAG_API_KEY") ?: "")
    set("realmEncryptionKey", System.getenv("REALM_ENCRYPTION_KEY") ?: "ZX06poC7a96dL9,FR_9|Ww<2%]?4Ij(3wR3DmyNj0[{(,8g%jX2{03P45_p`N6|2")
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
