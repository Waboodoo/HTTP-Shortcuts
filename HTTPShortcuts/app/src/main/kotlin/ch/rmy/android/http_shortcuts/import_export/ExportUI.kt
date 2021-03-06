package ch.rmy.android.http_shortcuts.import_export

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.logException
import ch.rmy.android.http_shortcuts.extensions.showMessageDialog
import ch.rmy.android.http_shortcuts.extensions.showSnackbar
import ch.rmy.android.http_shortcuts.extensions.startActivity
import ch.rmy.android.http_shortcuts.utils.Destroyable
import ch.rmy.android.http_shortcuts.utils.Destroyer
import ch.rmy.android.http_shortcuts.utils.FileUtil
import io.reactivex.android.schedulers.AndroidSchedulers

class ExportUI(private val activity: FragmentActivity) : Destroyable {

    private val context: Context = activity

    private val destroyer = Destroyer()

    fun showExportOptions(format: ExportFormat = ExportFormat.LEGACY_JSON, single: Boolean = false, intentHandler: (Intent) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            DialogBuilder(context)
                .title(R.string.title_export)
                .item(R.string.button_export_to_general) { openFilePickerForExport(format, single, intentHandler) }
                .item(R.string.button_export_send_to) { sendExport(format, single) }
                .showIfPossible()
        } else {
            sendExport(format, single)
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun openFilePickerForExport(format: ExportFormat = ExportFormat.LEGACY_JSON, single: Boolean, intentHandler: (Intent) -> Unit) {
        Intent(Intent.ACTION_CREATE_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType(format.fileType)
            .putExtra(Intent.EXTRA_TITLE, format.getFileName(single))
            .let(intentHandler)
    }

    fun startExport(
        uri: Uri,
        format: ExportFormat = ExportFormat.ZIP,
        shortcutId: String? = null,
        variableIds: Collection<String>? = null,
    ) {
        // TODO: Replace progress dialog with something better
        val progressDialog = ProgressDialog(activity).apply {
            setMessage(context.getString(R.string.export_in_progress))
            setCanceledOnTouchOutside(false)
        }
        Exporter(context.applicationContext)
            .exportToUri(uri, format, shortcutId, variableIds, excludeDefaults = true)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                progressDialog.show()
            }
            .doOnEvent { _, _ ->
                progressDialog.dismiss()
            }
            .subscribe({ status ->
                activity.showSnackbar(context.resources.getQuantityString(
                    R.plurals.shortcut_export_success,
                    status.exportedShortcuts,
                    status.exportedShortcuts
                ))
            }, { e ->
                activity.showMessageDialog(context.getString(R.string.export_failed_with_reason, e.message))
                logException(e)
            })
            .attachTo(destroyer)
    }

    private fun sendExport(format: ExportFormat = ExportFormat.LEGACY_JSON, single: Boolean = false) {
        val cacheFile = FileUtil.createCacheFile(context, format.getFileName(single))

        // TODO: Replace progress dialog with something better
        val progressDialog = ProgressDialog(activity).apply {
            setMessage(context.getString(R.string.export_in_progress))
            setCanceledOnTouchOutside(false)
        }
        Exporter(context.applicationContext)
            .exportToUri(cacheFile, excludeDefaults = true)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                progressDialog.show()
            }
            .doOnEvent { _, _ ->
                progressDialog.dismiss()
            }
            .subscribe(
                {
                    Intent(Intent.ACTION_SEND)
                        .setType(format.fileTypeForSharing)
                        .putExtra(Intent.EXTRA_STREAM, cacheFile)
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        .let {
                            Intent.createChooser(it, context.getString(R.string.title_export))
                        }
                        .startActivity(activity)
                },
                { error ->
                    logException(error)
                    activity.showSnackbar(R.string.error_generic)
                }
            )
            .attachTo(destroyer)
    }

    override fun destroy() {
        destroyer.destroy()
    }
}