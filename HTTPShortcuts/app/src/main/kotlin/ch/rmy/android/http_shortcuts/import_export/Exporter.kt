package ch.rmy.android.http_shortcuts.import_export

import android.content.Context
import android.net.Uri
import ch.rmy.android.framework.extensions.applyIf
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.runFor
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.utils.FileUtil
import ch.rmy.android.http_shortcuts.data.domains.app.AppRepository
import ch.rmy.android.http_shortcuts.data.domains.certificate_pins.CertificatePinRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.enums.ClientCertParams
import ch.rmy.android.http_shortcuts.data.models.Base
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.CertificatePin
import ch.rmy.android.http_shortcuts.data.models.FileUploadOptions
import ch.rmy.android.http_shortcuts.data.models.Header
import ch.rmy.android.http_shortcuts.data.models.Option
import ch.rmy.android.http_shortcuts.data.models.Parameter
import ch.rmy.android.http_shortcuts.data.models.Repetition
import ch.rmy.android.http_shortcuts.data.models.ResponseHandling
import ch.rmy.android.http_shortcuts.data.models.Section
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.data.models.WorkingDirectory
import ch.rmy.android.http_shortcuts.usecases.GetUsedCustomIconsUseCase
import ch.rmy.android.http_shortcuts.usecases.GetUsedWorkingDirectoryIdsUseCase
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import io.realm.kotlin.ext.copyFromRealm
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

class Exporter
@Inject
constructor(
    private val context: Context,
    private val appRepository: AppRepository,
    private val certificatePinRepository: CertificatePinRepository,
    private val getUsedCustomIcons: GetUsedCustomIconsUseCase,
    private val getUsedWorkingDirectoryIds: GetUsedWorkingDirectoryIdsUseCase,
) {

    suspend fun exportToUri(
        uri: Uri,
        format: ExportFormat = ExportFormat.ZIP,
        shortcutIds: Collection<ShortcutId>? = null,
        variableIds: Collection<VariableId>? = null,
        excludeDefaults: Boolean = false,
        excludeVariableValuesIfNeeded: Boolean = true,
    ): ExportStatus {
        val base = withContext(Dispatchers.Default) {
            getBase(shortcutIds, variableIds)
                .applyIf(excludeVariableValuesIfNeeded) {
                    variables.forEach { variable ->
                        if (variable.isExcludeValueFromExport) {
                            variable.value = ""
                        }
                    }
                }
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
        base: ImportExportBase,
        excludeDefaults: Boolean = false,
    ): ExportStatus {
        exportData(base, writer, excludeDefaults)
        return ExportStatus(exportedShortcuts = base.categories.sumOf { it.shortcuts.size })
    }

    private suspend fun getBase(
        shortcutIds: Collection<ShortcutId>?,
        variableIds: Collection<VariableId>?,
    ): ImportExportBase {
        val realmBase = appRepository.getBase().copyFromRealm()
        if (shortcutIds != null) {
            realmBase.title = null
            realmBase.categories.forEach { category ->
                category.shortcuts.removeIf { shortcut ->
                    shortcut.id !in shortcutIds
                }
            }
            realmBase.categories.removeIf { category ->
                category.shortcuts.isEmpty()
            }
        }
        if (variableIds != null) {
            realmBase.variables.removeIf { it.id !in variableIds }
        }

        getUsedWorkingDirectoryIds(realmBase).let { workingDirectoryIds ->
            realmBase.workingDirectories.removeIf { it.id !in workingDirectoryIds }
        }

        return ImportExportBase(
            version = realmBase.version,
            compatibilityVersion = realmBase.compatibilityVersion,
            categories = realmBase.categories,
            variables = realmBase.variables,
            certificatePins = certificatePinRepository.getCertificatePins(),
            workingDirectories = realmBase.workingDirectories,
            title = realmBase.title,
            globalCode = realmBase.globalCode,
        )
    }

    private suspend fun exportData(base: ImportExportBase, writer: Appendable, excludeDefaults: Boolean = false) {
        withContext(Dispatchers.IO) {
            try {
                val serializer = ModelSerializer()
                GsonUtil.gson
                    .newBuilder()
                    .setPrettyPrinting()
                    .runIf(excludeDefaults) {
                        runFor(MODEL_CLASSES) { clazz ->
                            registerTypeAdapter(clazz.java, serializer)
                        }
                    }
                    .create()
                    .toJson(base, writer)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                if (e !is NoClassDefFoundError) {
                    logException(e)
                }
                GsonUtil.gson
                    .newBuilder()
                    .setPrettyPrinting()
                    .create()
                    .toJson(base, writer)
            }
        }
    }

    private suspend fun getFilesToExport(context: Context, base: ImportExportBase, shortcutIds: Collection<ShortcutId>?): List<File> =
        getShortcutIconFiles(context, shortcutIds)
            .plus(getClientCertFiles(context, base, shortcutIds))
            .filter { it.exists() }
            .toList()

    private suspend fun getShortcutIconFiles(context: Context, shortcutIds: Collection<ShortcutId>?) =
        getUsedCustomIcons(shortcutIds)
            .mapNotNull {
                it.getFile(context)
            }

    private fun getClientCertFiles(context: Context, base: ImportExportBase, shortcutIds: Collection<ShortcutId>?) =
        base.categories
            .flatMap { it.shortcuts }
            .asSequence()
            .runIfNotNull(shortcutIds) { ids ->
                filter { shortcut -> shortcut.id in ids }
            }
            .mapNotNull { (it.clientCertParams as? ClientCertParams.File) }
            .map { it.getFile(context) }

    data class ExportStatus(val exportedShortcuts: Int)

    companion object {
        const val JSON_FILE = "shortcuts.json"

        private val MODEL_CLASSES = setOf(
            Base::class,
            Header::class,
            Parameter::class,
            Shortcut::class,
            Option::class,
            Variable::class,
            Category::class,
            Section::class,
            FileUploadOptions::class,
            ResponseHandling::class,
            Repetition::class,
            CertificatePin::class,
            WorkingDirectory::class,
        )
    }
}
