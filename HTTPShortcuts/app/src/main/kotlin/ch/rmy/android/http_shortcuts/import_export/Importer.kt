package ch.rmy.android.http_shortcuts.import_export

import android.content.Context
import android.net.Uri
import ch.rmy.android.framework.extensions.isWebUrl
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.utils.FileUtil
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.domains.import_export.ImportRepository
import ch.rmy.android.http_shortcuts.import_export.models.ImportExportBase
import ch.rmy.android.http_shortcuts.utils.GsonUtil.gson
import ch.rmy.android.http_shortcuts.utils.IconUtil
import ch.rmy.android.http_shortcuts.utils.NoCloseInputStream
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URISyntaxException
import java.net.URL
import java.util.zip.ZipException
import java.util.zip.ZipInputStream
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

class Importer
@Inject
constructor(
    private val context: Context,
    private val importMigrator: ImportMigrator,
    private val importRepository: ImportRepository,
    private val importExportDefaultsProvider: ImportExportDefaultsProvider,
) {
    suspend fun importFromUri(uri: Uri, importMode: ImportMode): ImportStatus =
        try {
            withContext(Dispatchers.IO) {
                val cacheFile = FileUtil.createCacheFile(context, IMPORT_TEMP_FILE)
                getStream(context, uri).use { inStream ->
                    FileUtil.getOutputStream(context, cacheFile).use { outStream ->
                        ensureActive()
                        inStream.copyTo(outStream)
                    }
                }

                try {
                    context.contentResolver.openInputStream(cacheFile)!!.use { stream ->
                        importFromZIP(stream, importMode)
                    }
                } catch (_: ZipException) {
                    context.contentResolver.openInputStream(cacheFile)!!.use { stream ->
                        importFromJSON(stream, importMode)
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw handleError(e)
        }

    private suspend fun importFromZIP(inputStream: InputStream, importMode: ImportMode): ImportStatus =
        withContext(Dispatchers.IO) {
            var importStatus: ImportStatus? = null
            ZipInputStream(inputStream).use { stream ->
                while (true) {
                    val entry = stream.nextEntry ?: break
                    when {
                        entry.name == Exporter.JSON_FILE -> {
                            importStatus = importFromJSON(NoCloseInputStream(stream), importMode)
                        }
                        IconUtil.isCustomIconName(entry.name) -> {
                            NoCloseInputStream(stream).copyTo(FileOutputStream(File(context.filesDir, entry.name)))
                        }
                        else -> {
                            stream.closeEntry()
                        }
                    }
                }
            }
            importStatus ?: throw ZipException("Invalid file")
        }

    suspend fun importFromJSON(inputStream: InputStream, importMode: ImportMode): ImportStatus =
        withContext(Dispatchers.IO) {
            val importData = BufferedReader(InputStreamReader(inputStream)).use { reader ->
                JsonParser.parseReader(reader)
            }
            logInfo("Starting import")
            val migratedImportData = importMigrator.migrate(importData)
            val importBase = importExportDefaultsProvider.applyDefaults(gson.fromJson(migratedImportData, ImportExportBase::class.java))
            try {
                importBase.validate()
                importRepository.import(importBase, importMode)
            } catch (e: IllegalArgumentException) {
                throw ImportException(e.message!!)
            }
            ImportStatus(
                importedShortcuts = importBase.categories?.sumOf { it.shortcuts?.size ?: 0 } ?: 0,
            )
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
            is JsonParseException -> {
                getString(R.string.import_failure_reason_invalid_json)
            }
            is ImportVersionMismatchException -> {
                getString(R.string.import_failure_reason_data_version_mismatch)
            }
            is InvalidFileException -> {
                // TODO: Localize this error message
                "Failed to import. The file doesn't seem to be of the right format."
            }
            is URISyntaxException,
            is IllegalArgumentException,
            is IllegalStateException,
            is IOException,
            -> {
                e.message
            }
            else ->
                e.cause
                    ?.takeIf { recursive }
                    ?.let {
                        getHumanReadableErrorMessage(it, recursive = false)
                    }
        }
    }

    data class ImportStatus(val importedShortcuts: Int)

    companion object {
        private const val IMPORT_TEMP_FILE = "import"
    }
}
