package ch.rmy.android.http_shortcuts.data.enums

enum class RequestBodyType(val type: String) {
    CUSTOM_TEXT("custom_text"),
    FORM_DATA("form_data"),
    X_WWW_FORM_URLENCODE("x_www_form_urlencode"),
    FILE("file"),
    ;

    override fun toString() =
        type

    companion object {
        fun parse(type: String): RequestBodyType? =
            entries.find { it.type == type }
    }
}
