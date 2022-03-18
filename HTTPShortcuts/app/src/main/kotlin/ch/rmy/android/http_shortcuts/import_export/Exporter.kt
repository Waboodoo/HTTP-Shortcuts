package ch.rmy.android.http_shortcuts.import_export

import android.content.Context
import android.net.Uri
import ch.rmy.android.framework.extensions.applyIf
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.mapFor
import ch.rmy.android.framework.extensions.mapIf
import ch.rmy.android.framework.extensions.safeRemoveIf
import ch.rmy.android.http_shortcuts.data.domains.app.AppRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.enums.ClientCertParams
import ch.rmy.android.http_shortcuts.data.models.BaseModel
import ch.rmy.android.http_shortcuts.data.models.CategoryModel
import ch.rmy.android.http_shortcuts.data.models.HeaderModel
import ch.rmy.android.http_shortcuts.data.models.OptionModel
import ch.rmy.android.http_shortcuts.data.models.ParameterModel
import ch.rmy.android.http_shortcuts.data.models.ResponseHandlingModel
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.data.models.VariableModel
import ch.rmy.android.http_shortcuts.usecases.GetUsedCustomIconsUseCase
import ch.rmy.android.http_shortcuts.utils.FileUtil
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class Exporter(private val context: Context) {

    private val appRepository = AppRepository()
    private val temporaryShortcutRepository = TemporaryShortcutRepository()
    private val getUsedCustomIcons = GetUsedCustomIconsUseCase(appRepository, temporaryShortcutRepository)

    fun exportToUri(
        uri: Uri,
        format: ExportFormat = ExportFormat.ZIP,
        shortcutId: String? = null,
        variableIds: Collection<String>? = null,
        excludeDefaults: Boolean = false,
    ): Single<ExportStatus> =
        getBase(shortcutId, variableIds)
            .map { base ->
                when (format) {
                    ExportFormat.ZIP -> {
                        ZipOutputStream(FileUtil.getOutputStream(context, uri)).use { out ->

                            out.putNextEntry(ZipEntry(JSON_FILE))
                            val writer = out.bufferedWriter()
                            val result = export(writer, base, excludeDefaults)
                            writer.flush()
                            out.closeEntry()

                            getFilesToExport(context, base).forEach { file ->
                                out.putNextEntry(ZipEntry(file.name))
                                FileInputStream(file).copyTo(out)
                                writer.flush()
                                out.closeEntry()
                            }
                            result
                        }
                    }
                    ExportFormat.LEGACY_JSON -> {
                        FileUtil.getWriter(context, uri).use { writer ->
                            export(writer, base, excludeDefaults)
                        }
                    }
                }
            }
            .subscribeOn(Schedulers.io())

    private fun export(
        writer: Appendable,
        base: BaseModel,
        excludeDefaults: Boolean = false,
    ): ExportStatus {
        exportData(base, writer, excludeDefaults)
        return ExportStatus(exportedShortcuts = base.shortcuts.size)
    }

    private fun getBase(shortcutId: String?, variableIds: Collection<String>?): Single<BaseModel> =
        appRepository.getBase()
            .map { base ->
                base.applyIf(shortcutId != null) {
                    title = null
                    categories.safeRemoveIf { category ->
                        category.shortcuts.none { it.id == shortcutId }
                    }
                    categories.firstOrNull()?.shortcuts?.safeRemoveIf { it.id != shortcutId }
                }
                    .applyIf(variableIds != null) {
                        variables.safeRemoveIf { !variableIds!!.contains(it.id) }
                    }
            }

    private fun exportData(base: BaseModel, writer: Appendable, excludeDefaults: Boolean = false) {
        try {
            val serializer = ModelSerializer()
            GsonUtil.gson
                .newBuilder()
                .setPrettyPrinting()
                .mapIf(excludeDefaults) {
                    mapFor(MODEL_CLASSES) { clazz ->
                        registerTypeAdapter(clazz, serializer)
                    }
                }
                .create()
                .toJson(base, writer)
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

    private fun getFilesToExport(context: Context, base: BaseModel): List<File> =
        getShortcutIconFiles(context)
            .plus(getClientCertFiles(context, base))
            .filter { it.exists() }
            .toList()

    private fun getShortcutIconFiles(context: Context) =
        getUsedCustomIcons()
            .blockingGet()
            .mapNotNull {
                it.getFile(context)
            }

    private fun getClientCertFiles(context: Context, base: BaseModel) =
        base.shortcuts.asSequence()
            .mapNotNull { (it.clientCertParams as? ClientCertParams.File) }
            .map { it.getFile(context) }

    data class ExportStatus(val exportedShortcuts: Int)

    companion object {
        const val JSON_FILE = "shortcuts.json"

        private val MODEL_CLASSES = setOf(
            BaseModel::class.java,
            HeaderModel::class.java,
            ParameterModel::class.java,
            ShortcutModel::class.java,
            OptionModel::class.java,
            VariableModel::class.java,
            CategoryModel::class.java,
            ResponseHandlingModel::class.java,
        )
    }
}
