package ch.rmy.android.http_shortcuts.import_export

import android.content.Context
import android.net.Uri
import androidx.core.text.isDigitsOnly
import ch.rmy.android.http_shortcuts.data.Controller
import ch.rmy.android.http_shortcuts.data.migration.ImportMigrator
import ch.rmy.android.http_shortcuts.data.models.Base
import ch.rmy.android.http_shortcuts.extensions.isWebUrl
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import ch.rmy.android.http_shortcuts.utils.UUIDUtils
import com.google.gson.JsonParser
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL

class Importer {

    fun import(context: Context, uri: Uri): Single<ImportStatus> =
        Single
            .create<ImportStatus> { emitter ->
                val inputStream = getStream(context, uri)
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val importData = JsonParser.parseReader(reader)
                    val migratedImportData = ImportMigrator.migrate(importData)
                    val newBase = GsonUtil.importData(migratedImportData)
                    validate(newBase)
                    Controller().use { controller ->
                        controller.importBaseSynchronously(newBase)
                    }
                    emitter.onSuccess(ImportStatus(
                        importedShortcuts = newBase.shortcuts.size,
                        needsRussianWarning = newBase.shortcuts.any { it.url.contains("https://api.beeline.ru/") }
                    ))
                }
            }
            .subscribeOn(Schedulers.io())

    private fun validate(base: Base) {
        if (base.categories.any { !UUIDUtils.isUUID(it.id) && !it.id.isDigitsOnly() }) {
            throw IllegalArgumentException("Invalid category ID found, must be UUID")
        }
    }

    private fun getStream(context: Context, uri: Uri): InputStream =
        if (uri.isWebUrl) {
            URL(uri.toString()).openStream()
        } else {
            context.contentResolver.openInputStream(uri)
                ?: throw IOException("Failed to open input stream")
        }

    data class ImportStatus(val importedShortcuts: Int, val needsRussianWarning: Boolean)

}