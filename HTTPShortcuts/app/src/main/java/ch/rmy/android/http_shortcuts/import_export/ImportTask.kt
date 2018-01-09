package ch.rmy.android.http_shortcuts.import_export

import android.content.Context
import android.net.Uri
import android.view.View
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import java.io.BufferedReader
import java.io.InputStreamReader

class ImportTask(context: Context, baseView: View) : SimpleTask<Uri>(context, baseView) {

    override fun doInBackground(vararg uris: Uri): Boolean? {
        val uri = uris[0]

        var controller: Controller? = null
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return false
            try {
                controller = Controller()
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val base = GsonUtil.importData(reader)
                    ImportMigrator.migrate(base)
                    controller.importBase(base)
                    LauncherShortcutManager.updateAppShortcuts(context, controller.categories)
                }
            } finally {
                controller?.destroy()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

        return true
    }

    override val progressMessage = getString(R.string.import_in_progress)

    override val successMessage = getString(R.string.import_success)

    override val failureMessage = getString(R.string.import_failed)

}