include(":app")
include(":framework")
include(":curl_command")
include(":favicon_grabber")
include(":icon_fetcher")
include(":scripting")
include(":shell_apk_template")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // Only include repos allowed by F-Droid
        // see https://f-droid.org/docs/Inclusion_Policy/
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        google()
    }
}
