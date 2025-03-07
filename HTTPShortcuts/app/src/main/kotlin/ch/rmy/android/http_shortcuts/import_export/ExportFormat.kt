package ch.rmy.android.http_shortcuts.import_export

enum class ExportFormat(val fileType: String, private val singleFileName: String, private val pluralFileName: String) {
    ZIP(fileType = "application/zip", singleFileName = "shortcut.zip", pluralFileName = "shortcuts.zip"),
    LEGACY_JSON(fileType = "application/json", singleFileName = "shortcut.json", pluralFileName = "shortcuts.json"),
    ;

    fun getFileName(single: Boolean) =
        if (single) singleFileName else pluralFileName

    val fileTypeForSharing
        get() = fileType.takeUnless { it == "application/json" } ?: "text/plain"
}
