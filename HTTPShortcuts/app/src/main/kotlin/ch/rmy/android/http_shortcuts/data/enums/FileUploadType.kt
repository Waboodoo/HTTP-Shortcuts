package ch.rmy.android.http_shortcuts.data.enums

enum class FileUploadType(val type: String) {
    FILE_PICKER("file_picker"),
    FILE_PICKER_MULTI("file_picker_multi"),
    CAMERA("camera"),
    FILE("stored_file"),
    STATIC_VALUE("static_value"),
    ;

    override fun toString() =
        type

    companion object {
        fun parse(type: String): FileUploadType? =
            entries.find { it.type == type }
    }
}
