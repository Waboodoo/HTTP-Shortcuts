package ch.rmy.android.http_shortcuts.import_export

import ch.rmy.android.http_shortcuts.data.Controller
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

class Exporter {

    fun export(path: String): Single<ExportStatus> =
        Single.create<ExportStatus> { emitter ->
            val base = Controller().use { controller ->
                controller.exportBase()
            }

            val file = getFile(path)
            BufferedWriter(FileWriter(file)).use {
                GsonUtil.exportData(base, it)
            }
            emitter.onSuccess(ExportStatus(
                exportedShortcuts = base.shortcuts.size
            ))
        }
            .observeOn(Schedulers.io())

    private fun getFile(directoryPath: String): File {
        val directory = File(directoryPath)

        var file = File(directory, FILE_NAME + FILE_EXTENSION)
        var count = 2
        while (file.exists()) {
            file = File(directory, FILE_NAME + count + FILE_EXTENSION)
            count++
        }
        return file
    }

    data class ExportStatus(val exportedShortcuts: Int)

    companion object {

        private const val FILE_NAME = "shortcuts"
        private const val FILE_EXTENSION = ".json"
    }

}
