package ch.rmy.android.http_shortcuts.import_export

import android.content.Context
import android.net.Uri
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.utils.FileUtil
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.variables.GlobalVariableId
import ch.rmy.android.http_shortcuts.data.enums.ClientCertParams
import ch.rmy.android.http_shortcuts.import_export.ImportExport.JSON_FILE
import ch.rmy.android.http_shortcuts.import_export.models.ExportBase
import ch.rmy.android.http_shortcuts.usecases.GetUsedCustomIconsUseCase
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import net.lingala.zip4j.io.outputstream.ZipOutputStream
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.AesKeyStrength
import net.lingala.zip4j.model.enums.CompressionMethod
import net.lingala.zip4j.model.enums.EncryptionMethod

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
        password: String? = null,
        shortcutIds: Collection<ShortcutId>? = null,
        globalVariableIds: Collection<GlobalVariableId>? = null,
        excludeDefaults: Boolean,
        excludeVariableValuesIfNeeded: Boolean = true,
    ): ExportStatus {
        val base = withContext(Dispatchers.Default) {
            exportBaseLoader.getBase(shortcutIds, globalVariableIds, excludeVariableValuesIfNeeded)
        }
        return withContext(Dispatchers.IO) {
            when (format) {
                ExportFormat.ZIP -> {
                    ZipOutputStream(FileUtil.getOutputStream(context, uri), password?.toCharArray()).use { out ->
                        val zipParameters = ZipParameters().apply {
                            compressionMethod = CompressionMethod.DEFLATE
                            if (password != null) {
                                isEncryptFiles = true
                                encryptionMethod = EncryptionMethod.AES
                                aesKeyStrength = AesKeyStrength.KEY_STRENGTH_256
                            }
                        }
                        zipParameters.fileNameInZip = JSON_FILE
                        out.putNextEntry(zipParameters)

                        val writer = out.bufferedWriter()
                        val exportStatus = export(writer, base, excludeDefaults)
                        writer.flush()
                        out.closeEntry()

                        getFilesToExport(context, base, shortcutIds).forEach { file ->
                            ensureActive()
                            zipParameters.fileNameInZip = file.name
                            out.putNextEntry(zipParameters)
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
}
