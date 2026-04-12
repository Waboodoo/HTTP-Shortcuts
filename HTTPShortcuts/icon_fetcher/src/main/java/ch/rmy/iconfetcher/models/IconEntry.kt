package ch.rmy.iconfetcher.models

data class IconEntry(
    val name: String,
    val tags: List<String>? = null,
    val aliases: List<String>? = null,
) {
    val url: String
        get() = "svg/$name.svg"
}
