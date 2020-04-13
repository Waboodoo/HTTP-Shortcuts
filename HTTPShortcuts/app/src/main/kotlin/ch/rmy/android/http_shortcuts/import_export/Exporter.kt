package ch.rmy.android.http_shortcuts.import_export

import android.content.Context
import android.net.Uri
import ch.rmy.android.http_shortcuts.data.Controller
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
                    GsonUtil.exportData(base, it)
                }
                emitter.onSuccess(ExportStatus(
                    exportedShortcuts = base.shortcuts.size
                ))
            }
            .subscribeOn(Schedulers.io())

    data class ExportStatus(val exportedShortcuts: Int)

}
