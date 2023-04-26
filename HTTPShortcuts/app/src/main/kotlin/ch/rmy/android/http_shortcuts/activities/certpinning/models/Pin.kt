package ch.rmy.android.http_shortcuts.activities.certpinning.models

import androidx.compose.runtime.Stable

@Stable
data class Pin(
    val id: String,
    val pattern: String,
    val hash: String,
) {
    @Stable
    fun formatted(): String =
        hash.chunked(2).joinToString(":")
}
