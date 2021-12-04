package ch.rmy.android.http_shortcuts.activities.settings

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.net.toUri
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.activities.remote_edit.RemoteEditActivity
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.dialogs.SpecialWarnings
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.isWebUrl
import ch.rmy.android.http_shortcuts.extensions.logException
import ch.rmy.android.http_shortcuts.extensions.showMessageDialog
import ch.rmy.android.http_shortcuts.extensions.showToast
import ch.rmy.android.http_shortcuts.extensions.startActivity
import ch.rmy.android.http_shortcuts.import_export.ExportFormat
import ch.rmy.android.http_shortcuts.import_export.ExportUI
import ch.rmy.android.http_shortcuts.import_export.ImportException
import ch.rmy.android.http_shortcuts.import_export.Importer
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.utils.FilePickerUtil
import ch.rmy.android.http_shortcuts.utils.Settings
import io.reactivex.android.schedulers.AndroidSchedulers

class ImportExportActivity : BaseActivity() {

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_import_export)
        setTitle(R.string.title_import_export)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_view, ImportExportFragment())
            .commit()
    }

    class ImportExportFragment : BaseSettingsFragment() {

        private val exportUI by lazy {
            destroyer.own(ExportUI(requireActivity()))
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.import_export, rootKey)

            initPreference("import_from_file") {
                openGeneralPickerForImport()
            }

            initPreference("import_from_url") {
                openImportUrlDialog()
            }

            initPreference("export") {
                exportUI.showExportOptions(format = ExportFormat.getPreferredFormat(requireContext())) { intent ->
                    try {
                        intent.startActivity(this, REQUEST_EXPORT_TO_DOCUMENTS)
                    } catch (e: ActivityNotFoundException) {
                        context?.showToast(R.string.error_not_supported)
                    }
                }
            }

            initPreference("remote_edit") {
                openRemoteEditor()
            }
        }

        private fun openGeneralPickerForImport() {
            try {
                FilePickerUtil.createIntent()
                    .startActivity(this, REQUEST_IMPORT_FROM_DOCUMENTS)
            } catch (e: ActivityNotFoundException) {
                requireActivity().showToast(R.string.error_not_supported)
            }
        }

        private fun openImportUrlDialog() {
            DialogBuilder(requireContext())
                .title(R.string.dialog_title_import_from_url)
                .textInput(
                    hint = "https://",
                    prefill = Settings(requireContext()).importUrl?.toString() ?: "",
                    allowEmpty = false,
                    callback = ::startImportFromURL,
                )
                .showIfPossible()
        }

        private fun startImportFromURL(url: String) {
            val uri = url.toUri()
            persistImportUrl(uri)
            if (uri.isWebUrl) {
                startImport(uri)
            } else {
                showMessageDialog(getString(R.string.import_failed_with_reason, getString(R.string.error_can_only_import_from_http_url)))
            }
        }

        private fun openRemoteEditor() {
            RemoteEditActivity.IntentBuilder(requireContext())
                .startActivity(this, REQUEST_REMOTE_EDIT)
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
            if (resultCode != RESULT_OK || intent == null) {
                return
            }
            when (requestCode) {
                REQUEST_EXPORT_TO_DOCUMENTS -> {
                    exportUI.startExport(
                        intent.data ?: return,
                        format = ExportFormat.getPreferredFormat(requireContext()),
                    )
                }
                REQUEST_IMPORT_FROM_DOCUMENTS -> {
                    startImport(intent.data ?: return)
                }
                REQUEST_REMOTE_EDIT -> {
                    reloadCategoriesWhenLeaving()
                }
            }
        }

        private fun persistImportUrl(url: Uri) {
            Settings(requireContext()).importUrl = url
        }

        private fun startImport(uri: Uri) {
            // TODO: Replace progress dialog with something better
            val progressDialog = ProgressDialog(activity).apply {
                setMessage(getString(R.string.import_in_progress))
                setCanceledOnTouchOutside(false)
            }
            Importer(requireContext().applicationContext)
                .importFromUri(uri, importMode = Importer.ImportMode.MERGE)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    progressDialog.show()
                }
                .doOnEvent { _, _ ->
                    progressDialog.dismiss()
                }
                .subscribe({ status ->
                    if (status.needsRussianWarning) {
                        SpecialWarnings.show(requireContext())
                    }
                    showSnackbar(
                        requireContext().resources.getQuantityString(
                            R.plurals.shortcut_import_success,
                            status.importedShortcuts,
                            status.importedShortcuts,
                        )
                    )
                    reloadCategoriesWhenLeaving()
                }, { e ->
                    if (e !is ImportException) {
                        logException(e)
                    }
                    showMessageDialog(getString(R.string.import_failed_with_reason, e.message))
                })
                .attachTo(destroyer)
        }

        private fun reloadCategoriesWhenLeaving() {
            requireActivity().setResult(
                Activity.RESULT_OK,
                Intent().apply {
                    putExtra(EXTRA_CATEGORIES_CHANGED, true)
                }
            )
        }

        override fun onDestroy() {
            super.onDestroy()
            destroyer.destroy()
        }
    }

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, ImportExportActivity::class.java)

    companion object {

        const val EXTRA_CATEGORIES_CHANGED = "categories_changed"

        private const val REQUEST_EXPORT_TO_DOCUMENTS = 2
        private const val REQUEST_IMPORT_FROM_DOCUMENTS = 3
        private const val REQUEST_REMOTE_EDIT = 4
    }
}
