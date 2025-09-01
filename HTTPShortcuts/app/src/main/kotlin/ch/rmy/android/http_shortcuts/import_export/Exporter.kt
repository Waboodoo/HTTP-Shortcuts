package ch.rmy.android.http_shortcuts.import_export

import android.content.Context
import android.net.Uri
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.utils.FileUtil
import ch.rmy.android.http_shortcuts.data.domains.variables.GlobalVariableId
import ch.rmy.android.http_shortcuts.data.enums.ClientCertParams
import ch.rmy.android.http_shortcuts.import_export.ImportExport.JSON_FILE
import ch.rmy.android.http_shortcuts.import_export.models.ExportBase
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import net.lingala.zip4j.io.outputstream.ZipOutputStream
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.AesKeyStrength
import net.lingala.zip4j.model.enums.CompressionMethod
import net.lingala.zip4j.model.enums.EncryptionMethod
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject

class Exporter
@Inject
constructor(
    private val context: Context,
    private val exportBaseLoader: ExportBaseLoader,
    private val importExportDefaultsProvider: ImportExportDefaultsProvider,
) {
    suspend fun exportToUri(
        uri: Uri,
        format: ExportFormat = ExportFormat.ZIP,
        password: String? = null,
        globalVariableIds: Collection<GlobalVariableId>? = null,
        excludeDefaults: Boolean,
        excludeVariableValuesIfNeeded: Boolean = true,
    ): ExportStatus {
        val base = withContext(Dispatchers.Default) {
            exportBaseLoader.getBase(globalVariableIds, excludeVariableValuesIfNeeded)
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

                        getFilesToExport(context, base).forEach { file ->
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

    private suspend fun getFilesToExport(context: Context, base: ExportBase): List<File> =
        getShortcutIconFiles(context)
            .plus(getClientCertFiles(context, base))
            .filter { it.exists() }
            .toList()

    private suspend fun getShortcutIconFiles(context: Context): List<File> =
        context.filesDir.listFiles {
            it.name.endsWith(".png") || it.name.endsWith(".jpg")
        }
            ?.toList()
            ?: emptyList()

    private fun getClientCertFiles(context: Context, base: ExportBase) =
        (base.categories ?: emptyList())
            .flatMap { it.shortcuts ?: emptyList() }
            .asSequence()
            .mapNotNull { ClientCertParams.parse(it.clientCert ?: "") as? ClientCertParams.File }
            .map { it.getFile(context) }

    data class ExportStatus(val exportedShortcuts: Int)
}
