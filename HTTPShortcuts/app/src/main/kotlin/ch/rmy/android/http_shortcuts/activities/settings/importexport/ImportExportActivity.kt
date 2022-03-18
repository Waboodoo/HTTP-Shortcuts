package ch.rmy.android.http_shortcuts.activities.settings.importexport

import android.app.Activity
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.net.toUri
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.createIntent
import ch.rmy.android.framework.extensions.initialize
import ch.rmy.android.framework.extensions.isWebUrl
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.observe
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.framework.utils.FilePickerUtil
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.activities.remote_edit.RemoteEditActivity
import ch.rmy.android.http_shortcuts.activities.settings.BaseSettingsFragment
import ch.rmy.android.http_shortcuts.import_export.ExportFormat
import ch.rmy.android.http_shortcuts.import_export.ExportUI
import ch.rmy.android.http_shortcuts.import_export.ImportException
import ch.rmy.android.http_shortcuts.import_export.Importer
import ch.rmy.android.http_shortcuts.utils.Settings
import io.reactivex.android.schedulers.AndroidSchedulers

class ImportExportActivity : BaseActivity() {

    private val viewModel: ImportExportViewModel by bindViewModel()
    private lateinit var fragment: ImportExportFragment

    override fun onCreated(savedState: Bundle?) {
        viewModel.initialize()
        initViews(savedState == null)
        initViewModelBindings()
    }

    private fun initViews(firstInit: Boolean) {
        setContentView(R.layout.activity_import_export)
        setTitle(R.string.title_import_export)
        if (firstInit) {
            fragment = ImportExportFragment()
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_view, fragment)
                .commit()
        } else {
            fragment = supportFragmentManager.findFragmentById(R.id.settings_view) as ImportExportFragment
        }
    }

    private fun initViewModelBindings() {
        viewModel.viewState.observe(this) { viewState ->
            setDialogState(viewState.dialogState, viewModel)
        }
        viewModel.events.observe(this, ::handleEvent)
    }

    override fun handleEvent(event: ViewModelEvent) {
        when (event) {
            is ImportExportEvent.StartImportFromURL -> fragment.startImportFromURL(event.url)
            else -> super.handleEvent(event)
        }
    }

    class ImportExportFragment : BaseSettingsFragment() {

        private val viewModel: ImportExportViewModel
            get() = (activity as ImportExportActivity).viewModel

        private val exportUI by lazy {
            destroyer.own(ExportUI(requireActivity()))
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.import_export, rootKey)

            initPreference("import_from_file") {
                openGeneralPickerForImport()
            }

            initPreference("import_from_url") {
                viewModel.onImportFromURLButtonClicked()
            }

            initPreference("export") {

                exportUI.showExportOptions(format = getExportFormat()) { intent ->
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

        private fun getExportFormat() =
            if (Settings(requireContext()).useLegacyExportFormat) ExportFormat.LEGACY_JSON else ExportFormat.ZIP

        private fun openGeneralPickerForImport() {
            try {
                FilePickerUtil.createIntent()
                    .startActivity(this, REQUEST_IMPORT_FROM_DOCUMENTS)
            } catch (e: ActivityNotFoundException) {
                requireActivity().showToast(R.string.error_not_supported)
            }
        }

        fun startImportFromURL(url: String) {
            val uri = url.toUri()
            persistImportUrl(uri)
            if (uri.isWebUrl) {
                startImport(uri)
            } else {
                viewModel.onImportFailedDueToInvalidUrl()
            }
        }

        private fun openRemoteEditor() {
            RemoteEditActivity.IntentBuilder()
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
                        format = getExportFormat(),
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
                    viewModel.onImportFailed(e.message ?: e::class.java.simpleName)
                })
                .attachTo(destroyer)
        }

        private fun reloadCategoriesWhenLeaving() {
            requireActivity().setResult(
                Activity.RESULT_OK,
                createIntent {
                    putExtra(EXTRA_CATEGORIES_CHANGED, true)
                },
            )
        }

        override fun onDestroy() {
            super.onDestroy()
            destroyer.destroy()
        }
    }

    class IntentBuilder : BaseIntentBuilder(ImportExportActivity::class.java)

    companion object {

        const val EXTRA_CATEGORIES_CHANGED = "categories_changed"

        private const val REQUEST_EXPORT_TO_DOCUMENTS = 2
        private const val REQUEST_IMPORT_FROM_DOCUMENTS = 3
        private const val REQUEST_REMOTE_EDIT = 4
    }
}
