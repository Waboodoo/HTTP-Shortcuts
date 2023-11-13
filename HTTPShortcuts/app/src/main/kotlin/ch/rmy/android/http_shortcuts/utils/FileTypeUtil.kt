package ch.rmy.android.http_shortcuts.utils

object FileTypeUtil {
    const val TYPE_XML = "text/xml"
    const val TYPE_XML_ALT = "application/xml"
    const val TYPE_JSON = "application/json"
    const val TYPE_HTML = "text/html"
    const val TYPE_YAML = "text/yaml"
    const val TYPE_YAML_ALT = "application/x-yaml"
    const val TYPE_PLAIN_TEXT = "text/plain"

    fun isImage(type: String?) = type?.startsWith("image/") == true
}
