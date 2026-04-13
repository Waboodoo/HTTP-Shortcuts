package ch.rmy.android.http_shortcuts.activities.contact

import androidx.annotation.Keep

@Keep
data class MetaData(
    val androidSdkVersion: Int,
    val appVersionCode: Long,
    val device: String,
    val language: String,
    val deviceId: String,
    val buildType: String,
)
