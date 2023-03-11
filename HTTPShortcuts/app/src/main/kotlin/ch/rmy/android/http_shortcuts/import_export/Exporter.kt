package ch.rmy.android.http_shortcuts.import_export

import android.content.Context
import android.net.Uri
import ch.rmy.android.framework.extensions.applyIfNotNull
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.runFor
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.extensions.safeRemoveIf
import ch.rmy.android.framework.utils.FileUtil
import ch.rmy.android.http_shortcuts.data.domains.app.AppRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.enums.ClientCertParams
import ch.rmy.android.http_shortcuts.data.models.Base
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.Header
import ch.rmy.android.http_shortcuts.data.models.Option
import ch.rmy.android.http_shortcuts.data.models.Parameter
import ch.rmy.android.http_shortcuts.data.models.ResponseHandling
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.usecases.GetUsedCustomIconsUseCase
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject

class Exporter
@Inject
constructor(
    private val context: Context,
    private val appRepository: AppRepository,
    private val getUsedCustomIcons: GetUsedCustomIconsUseCase,
) {

    suspend fun exportToUri(
        uri: Uri,
        format: ExportFormat = ExportFormat.ZIP,
        shortcutIds: Collection<ShortcutId>? = null,
        variableIds: Collection<VariableId>? = null,
        excludeDefaults: Boolean = false,
    ): ExportStatus {
        val base = getBase(shortcutIds, variableIds)
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
        base: Base,
        excludeDefaults: Boolean = false,
    ): ExportStatus {
        exportData(base, writer, excludeDefaults)
        return ExportStatus(exportedShortcuts = base.shortcuts.size)
    }

    private suspend fun getBase(shortcutIds: Collection<ShortcutId>?, variableIds: Collection<VariableId>?): Base =
        appRepository.getBase()
            .applyIfNotNull(shortcutIds) {
                title = null
                categories.forEach { category ->
                    category.shortcuts.safeRemoveIf { shortcut ->
                        shortcut.id !in shortcutIds!!
                    }
                }
                categories.safeRemoveIf { category ->
                    category.shortcuts.isEmpty()
                }
            }
            .applyIfNotNull(variableIds) {
                variables.safeRemoveIf { !variableIds!!.contains(it.id) }
            }

    private suspend fun exportData(base: Base, writer: Appendable, excludeDefaults: Boolean = false) {
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

    private suspend fun getFilesToExport(context: Context, base: Base, shortcutIds: Collection<ShortcutId>?): List<File> =
        getShortcutIconFiles(context, shortcutIds)
            .plus(getClientCertFiles(context, base, shortcutIds))
            .filter { it.exists() }
            .toList()

    private suspend fun getShortcutIconFiles(context: Context, shortcutIds: Collection<ShortcutId>?) =
        getUsedCustomIcons(shortcutIds)
            .mapNotNull {
                it.getFile(context)
            }

    private fun getClientCertFiles(context: Context, base: Base, shortcutIds: Collection<ShortcutId>?) =
        base.shortcuts.asSequence()
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
            ResponseHandling::class,
        )
    }
}
