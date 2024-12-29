include(":app")
include(":curl_command")
include(":favicon_grabber")
include(":scripting")

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
