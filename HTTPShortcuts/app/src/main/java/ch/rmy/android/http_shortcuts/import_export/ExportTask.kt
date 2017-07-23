package ch.rmy.android.http_shortcuts.import_export

import android.content.Context
import android.view.View
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import java.io.*

class ExportTask(context: Context, baseView: View) : SimpleTask<String>(context, baseView) {

    override fun doInBackground(vararg path: String): Boolean? {
        val controller = Controller()
        val base = controller.exportBase()
        controller.destroy()

        val file = getFile(path[0])
        var writer: Writer? = null
        try {
            try {
                writer = BufferedWriter(FileWriter(file))
                GsonUtil.exportData(base, writer)
            } finally {
                writer?.close()
            }
        } catch (e: IOException) {
            return false
        }

        return true
    }

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

    override val progressMessage = getString(R.string.export_in_progress)

    override val successMessage = getString(R.string.export_success)

    override val failureMessage = getString(R.string.export_failed)

    companion object {

        private const val FILE_NAME = "shortcuts"
        private const val FILE_EXTENSION = ".json"
    }

}
