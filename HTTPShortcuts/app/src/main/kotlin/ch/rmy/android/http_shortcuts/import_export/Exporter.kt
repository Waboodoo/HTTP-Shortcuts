package ch.rmy.android.http_shortcuts.import_export

import android.content.Context
import android.net.Uri
import ch.rmy.android.http_shortcuts.data.Controller
import ch.rmy.android.http_shortcuts.data.models.Base
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.Header
import ch.rmy.android.http_shortcuts.data.models.Option
import ch.rmy.android.http_shortcuts.data.models.Parameter
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.BufferedWriter
import java.io.OutputStreamWriter

class Exporter(private val context: Context) {

    fun export(uri: Uri): Single<ExportStatus> =
        Single
            .create<ExportStatus> { emitter ->
                val base = Controller().use { controller ->
                    controller.exportBase()
                }
                BufferedWriter(
                    OutputStreamWriter(context.contentResolver.openOutputStream(uri, "w")!!)
                ).use {
                    exportData(base, it)
                }
                emitter.onSuccess(ExportStatus(
                    exportedShortcuts = base.shortcuts.size
                ))
            }
            .subscribeOn(Schedulers.io())

    private fun exportData(base: Base, writer: Appendable) {
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
            .create()
            .toJson(base, writer)
    }

    data class ExportStatus(val exportedShortcuts: Int)

}
