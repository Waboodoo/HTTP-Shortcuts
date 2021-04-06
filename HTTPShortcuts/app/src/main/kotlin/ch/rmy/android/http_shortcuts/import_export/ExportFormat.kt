package ch.rmy.android.http_shortcuts.import_export

import android.content.Context
import ch.rmy.android.http_shortcuts.utils.Settings

enum class ExportFormat(val fileType: String, private val singleFileName: String, private val pluralFileName: String) {
    ZIP(fileType = "application/zip", singleFileName = "shortcut.zip", pluralFileName = "shortcuts.zip"),
    LEGACY_JSON(fileType = "application/json", singleFileName = "shortcut.json", pluralFileName = "shortcuts.json");

    fun getFileName(single: Boolean) =
        if (single) singleFileName else pluralFileName

    val fileTypeForSharing
        get() = fileType.takeUnless { it == "application/json" } ?: "text/plain"

    companion object {
        fun getPreferredFormat(context: Context) =
            if (Settings(context).useLegacyExportFormat) LEGACY_JSON else ZIP
    }
}