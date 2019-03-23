package ch.rmy.android.http_shortcuts.import_export

import android.content.Context
import android.net.Uri
import android.view.View
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import com.google.gson.JsonParser
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class ImportTask(context: Context, baseView: View) : SimpleTask<Uri>(context, baseView) {

    override fun doInBackground(vararg uris: Uri): Exception? {
        val uri = uris[0]

        Controller().use { controller ->
            return try {
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: return IOException("Failed to open input stream")
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val importData = JsonParser().parse(reader)

                    val migratedImportData = ImportMigrator.migrate(importData)
                    val newBase = GsonUtil.importData(migratedImportData)
                    controller.importBaseSynchronously(newBase)
                    LauncherShortcutManager.updateAppShortcuts(context, controller.getCategories())
                }
                null
            } catch (e: Exception) {
                e
            }
        }
    }

    override val progressMessage = getString(R.string.import_in_progress)

    override val successMessage = getString(R.string.import_success)

    override val failureMessage = getString(R.string.import_failed)

}