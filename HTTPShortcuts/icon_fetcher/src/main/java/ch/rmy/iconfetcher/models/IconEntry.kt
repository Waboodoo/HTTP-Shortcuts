package ch.rmy.iconfetcher.models

import ch.rmy.iconfetcher.Config.ICONS_BASE_URL

data class IconEntry(
    val name: String,
    val tags: List<String>? = null,
    val aliases: List<String>? = null,
) {
    val url: String
        get() = "${ICONS_BASE_URL}svg/$name.svg"
}
