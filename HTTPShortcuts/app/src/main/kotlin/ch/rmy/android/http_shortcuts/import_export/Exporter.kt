package ch.rmy.android.http_shortcuts.import_export

import android.content.Context
import android.net.Uri
import ch.rmy.android.http_shortcuts.data.RealmFactory
import ch.rmy.android.http_shortcuts.data.Repository
import ch.rmy.android.http_shortcuts.data.models.Base
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.Header
import ch.rmy.android.http_shortcuts.data.models.Option
import ch.rmy.android.http_shortcuts.data.models.Parameter
import ch.rmy.android.http_shortcuts.data.models.ResponseHandling
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.extensions.detachFromRealm
import ch.rmy.android.http_shortcuts.extensions.logException
import ch.rmy.android.http_shortcuts.extensions.mapIf
import ch.rmy.android.http_shortcuts.utils.FileUtil
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import ch.rmy.android.http_shortcuts.utils.RxUtils
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class Exporter(private val context: Context) {

    fun exportToUri(
        uri: Uri,
        shortcutId: String? = null,
        variableIds: Collection<String>? = null,
        excludeDefaults: Boolean = false,
    ): Single<ExportStatus> =
        RxUtils
            .single {
                FileUtil.getWriter(context, uri).use {
                    export(it, shortcutId, variableIds, excludeDefaults)
                }
            }
            .subscribeOn(Schedulers.io())

    fun export(
        writer: Appendable,
        shortcutId: String? = null,
        variableIds: Collection<String>? = null,
        excludeDefaults: Boolean = false,
    ): ExportStatus {
        val base = getDetachedBase(shortcutId, variableIds)
        exportData(base, writer, excludeDefaults)
        return ExportStatus(exportedShortcuts = base.shortcuts.size)
    }

    private fun getDetachedBase(shortcutId: String?, variableIds: Collection<String>?): Base =
        RealmFactory.withRealm { realm ->
            Repository.getBase(realm)!!.detachFromRealm()
        }
            .also { base ->
                if (shortcutId != null) {
                    base.title = null
                    base.categories.removeIf {
                        it.shortcuts.none { it.id == shortcutId }
                    }
                    base.categories.firstOrNull()?.shortcuts?.removeIf { it.id != shortcutId }
                }
                if (variableIds != null) {
                    base.variables.removeIf { !variableIds.contains(it.id) }
                }
            }

    private fun exportData(base: Base, writer: Appendable, excludeDefaults: Boolean = false) {
        try {
            val serializer = ModelSerializer()
            GsonUtil.gson
                .newBuilder()
                .setPrettyPrinting()
                .mapIf(excludeDefaults) {
                    it.registerTypeAdapter(Base::class.java, serializer)
                        .registerTypeAdapter(Header::class.java, serializer)
                        .registerTypeAdapter(Parameter::class.java, serializer)
                        .registerTypeAdapter(Shortcut::class.java, serializer)
                        .registerTypeAdapter(Option::class.java, serializer)
                        .registerTypeAdapter(Variable::class.java, serializer)
                        .registerTypeAdapter(Category::class.java, serializer)
                        .registerTypeAdapter(ResponseHandling::class.java, serializer)
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

    data class ExportStatus(val exportedShortcuts: Int)

}
