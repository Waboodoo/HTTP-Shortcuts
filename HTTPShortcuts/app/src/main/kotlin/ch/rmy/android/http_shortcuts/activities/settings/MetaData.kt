package ch.rmy.android.http_shortcuts.activities.settings

data class MetaData(
    val androidSdkVersion: Int,
    val appVersion: String,
    val device: String,
    val language: String,
    val userId: String,
)
