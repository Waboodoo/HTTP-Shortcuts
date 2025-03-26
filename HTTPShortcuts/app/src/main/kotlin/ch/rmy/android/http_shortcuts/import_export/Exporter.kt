package ch.rmy.android.http_shortcuts.import_export

import android.content.Context
import android.net.Uri
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.utils.FileUtil
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.enums.ClientCertParams
import ch.rmy.android.http_shortcuts.import_export.models.ExportBase
import ch.rmy.android.http_shortcuts.usecases.GetUsedCustomIconsUseCase
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

class Exporter
@Inject
constructor(
    private val context: Context,
    private val exportBaseLoader: ExportBaseLoader,
    private val importExportDefaultsProvider: ImportExportDefaultsProvider,
    private val getUsedCustomIcons: GetUsedCustomIconsUseCase,
) {
    suspend fun exportToUri(
        uri: Uri,
        format: ExportFormat = ExportFormat.ZIP,
        shortcutIds: Collection<ShortcutId>? = null,
        variableIds: Collection<VariableId>? = null,
        excludeDefaults: Boolean,
        excludeVariableValuesIfNeeded: Boolean = true,
    ): ExportStatus {
        val base = withContext(Dispatchers.Default) {
            exportBaseLoader.getBase(shortcutIds, variableIds, excludeVariableValuesIfNeeded)
        }
        return withContext(Dispatchers.IO) {
            when (format) {
                ExportFormat.ZIP -> {
                    ZipOutputStream(FileUtil.getOutputStream(context, uri)).use { out ->
                        out.putNextEntry(ZipEntry(JSON_FILE))
                        val writer = out.bufferedWriter()
                        val exportStatus = export(writer, base, excludeDefaults)
                        writer.flush()
                        out.closeEntry()

                        getFilesToExport(context, base, shortcutIds).forEach { file ->
                            ensureActive()
                            out.putNextEntry(ZipEntry(file.name))
                            FileInputStream(file).copyTo(out)
                            writer.flush()
                            out.closeEntry()
                        }
                        exportStatus
                    }
                }
                ExportFormat.LEGACY_JSON -> {
                    FileUtil.getWriter(context, uri).use { writer ->
                        export(writer, base, excludeDefaults)
                    }
                }
            }
        }
    }

    private suspend fun export(
        writer: Appendable,
        base: ExportBase,
        excludeDefaults: Boolean = false,
    ): ExportStatus {
        exportData(base, writer, excludeDefaults)
        return ExportStatus(exportedShortcuts = base.categories?.sumOf { it.shortcuts?.size ?: 0 } ?: 0)
    }

    private suspend fun exportData(base: ExportBase, writer: Appendable, excludeDefaults: Boolean = false) {
        withContext(Dispatchers.IO) {
            GsonUtil.gson
                .newBuilder()
                .setPrettyPrinting()
                .runIf(!excludeDefaults) {
                    serializeNulls()
                }
                .create()
                .toJson(
                    if (excludeDefaults) base else importExportDefaultsProvider.applyDefaults(base),
                    writer,
                )
        }
    }

    private suspend fun getFilesToExport(context: Context, base: ExportBase, shortcutIds: Collection<ShortcutId>?): List<File> =
        getShortcutIconFiles(context, shortcutIds)
            .plus(getClientCertFiles(context, base, shortcutIds))
            .filter { it.exists() }
            .toList()

    private suspend fun getShortcutIconFiles(context: Context, shortcutIds: Collection<ShortcutId>?) =
        getUsedCustomIcons(shortcutIds)
            .mapNotNull {
                it.getFile(context)
            }

    private fun getClientCertFiles(context: Context, base: ExportBase, shortcutIds: Collection<ShortcutId>?) =
        (base.categories ?: emptyList())
            .flatMap { it.shortcuts ?: emptyList() }
            .asSequence()
            .runIfNotNull(shortcutIds) { ids ->
                filter { shortcut -> shortcut.id in ids }
            }
            .mapNotNull { ClientCertParams.parse(it.clientCert ?: "") as? ClientCertParams.File }
            .map { it.getFile(context) }

    data class ExportStatus(val exportedShortcuts: Int)

    companion object {
        const val JSON_FILE = "shortcuts.json"
    }
}
