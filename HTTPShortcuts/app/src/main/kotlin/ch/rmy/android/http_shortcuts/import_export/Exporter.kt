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
import ch.rmy.android.http_shortcuts.utils.FileUtil
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import ch.rmy.android.http_shortcuts.utils.RxUtils
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class Exporter(private val context: Context) {

    fun export(uri: Uri): Single<ExportStatus> =
        RxUtils
            .single {
                val base = getDetachedBase()
                FileUtil.getWriter(context, uri).use {
                    exportData(base, it)
                }
                ExportStatus(exportedShortcuts = base.shortcuts.size)
            }
            .subscribeOn(Schedulers.io())

    private fun getDetachedBase(): Base =
        RealmFactory.withRealm { realm ->
            Repository.getBase(realm)!!.detachFromRealm()
        }

    private fun exportData(base: Base, writer: Appendable) {
        try {
            val serializer = ModelSerializer()
            GsonUtil.gson
                .newBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Base::class.java, serializer)
                .registerTypeAdapter(Header::class.java, serializer)
                .registerTypeAdapter(Parameter::class.java, serializer)
                .registerTypeAdapter(Shortcut::class.java, serializer)
                .registerTypeAdapter(Option::class.java, serializer)
                .registerTypeAdapter(Variable::class.java, serializer)
                .registerTypeAdapter(Category::class.java, serializer)
                .registerTypeAdapter(ResponseHandling::class.java, serializer)
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
