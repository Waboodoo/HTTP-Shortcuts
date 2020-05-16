package ch.rmy.android.http_shortcuts.import_export

import android.content.Context
import android.net.Uri
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.Controller
import ch.rmy.android.http_shortcuts.data.migration.ImportMigrator
import ch.rmy.android.http_shortcuts.data.models.Base
import ch.rmy.android.http_shortcuts.extensions.isWebUrl
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import ch.rmy.android.http_shortcuts.utils.RxUtils
import ch.rmy.android.http_shortcuts.utils.UUIDUtils
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URISyntaxException
import java.net.URL

class Importer(private val context: Context) {

    fun import(uri: Uri): Single<ImportStatus> =
        RxUtils
            .single<ImportStatus> {
                val inputStream = getStream(context, uri)
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val importData = JsonParser.parseReader(reader)
                    val migratedImportData = ImportMigrator.migrate(importData)
                    val newBase = GsonUtil.importData(migratedImportData)
                    validate(newBase)
                    Controller().use { controller ->
                        controller.importBaseSynchronously(newBase)
                    }
                    ImportStatus(
                        importedShortcuts = newBase.shortcuts.size,
                        needsRussianWarning = newBase.shortcuts.any { it.url.contains(".beeline.ru") }
                    )
                }
            }
            .onErrorResumeNext { error ->
                Single.error(handleError(error))
            }
            .subscribeOn(Schedulers.io())

    private fun validate(base: Base) {
        if (base.categories.any { category ->
                category.shortcuts.any { shortcut ->
                    !UUIDUtils.isUUID(shortcut.id) && shortcut.id.toIntOrNull() == null
                }
            }) {
            throw IllegalArgumentException("Invalid shortcut ID found, must be UUID")
        }
        if (base.categories.any { category ->
                !UUIDUtils.isUUID(category.id) && category.id.toIntOrNull() == null
            }) {
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

    private fun handleError(error: Throwable): Throwable =
        getHumanReadableErrorMessage(error)
            ?.let { ImportException(it) }
            ?: error

    private fun getHumanReadableErrorMessage(e: Throwable, recursive: Boolean = true): String? = with(context) {
        when (e) {
            is JsonParseException, is JsonSyntaxException -> {
                getString(R.string.import_failure_reason_invalid_json)
            }
            is URISyntaxException,
            is IllegalArgumentException,
            is IllegalStateException,
            is IOException -> {
                e.message
            }
            else -> e.cause
                ?.takeIf { recursive }
                ?.let {
                    getHumanReadableErrorMessage(it, recursive = false)
                }
        }
    }

    data class ImportStatus(val importedShortcuts: Int, val needsRussianWarning: Boolean)

}