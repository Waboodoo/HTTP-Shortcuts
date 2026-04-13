package ch.rmy.iconfetcher.models

import androidx.annotation.Keep

@Keep
data class IconEntry(
    val name: String,
    val tags: List<String>? = null,
    val aliases: List<String>? = null,
) {
    val url: String
        get() = "svg/$name.svg"
}
