package ch.rmy.android.http_shortcuts.activities.settings.importexport

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.createIntent
import ch.rmy.android.framework.extensions.initialize
import ch.rmy.android.framework.extensions.launch
import ch.rmy.android.framework.extensions.observe
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.framework.ui.BaseActivityResultContract
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.framework.utils.FilePickerUtil
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.activities.remote_edit.RemoteEditActivity
import ch.rmy.android.http_shortcuts.activities.settings.BaseSettingsFragment
import ch.rmy.android.http_shortcuts.import_export.ExportFormat
import ch.rmy.android.http_shortcuts.import_export.OpenFilePickerForExportContract

class ImportExportActivity : BaseActivity() {

    private val openFilePickerForExport = registerForActivityResult(OpenFilePickerForExportContract) { fileUri ->
        fileUri?.let(viewModel::onFilePickedForExport)
    }

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
            is ImportExportEvent.OpenFilePickerForExport -> openFilePickerForExport(event.exportFormat)
            else -> super.handleEvent(event)
        }
    }

    private fun openFilePickerForExport(exportFormat: ExportFormat) {
        try {
            openFilePickerForExport.launch(
                OpenFilePickerForExportContract.Params(
                    format = exportFormat,
                    single = false,
                )
            )
        } catch (e: ActivityNotFoundException) {
            context.showToast(R.string.error_not_supported)
        }
    }

    class ImportExportFragment : BaseSettingsFragment() {

        private val openFilePickerForImport = registerForActivityResult(FilePickerUtil.PickFile) { fileUri ->
            fileUri?.let(viewModel::onFilePickedForImport)
        }

        private val openRemoteEditor = registerForActivityResult(RemoteEditActivity.OpenRemoteEditor) { changesImported ->
            viewModel.onRemoteEditorClosed(changesImported)
        }

        private val viewModel: ImportExportViewModel
            get() = (activity as ImportExportActivity).viewModel

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.import_export, rootKey)

            initPreference("import_from_file") {
                openGeneralPickerForImport()
            }

            initPreference("import_from_url") {
                viewModel.onImportFromURLButtonClicked()
            }

            initPreference("export") {
                viewModel.onExportButtonClicked()
            }

            initPreference("remote_edit") {
                openRemoteEditor.launch()
            }
        }

        private fun openGeneralPickerForImport() {
            try {
                openFilePickerForImport.launch(null)
            } catch (e: ActivityNotFoundException) {
                requireActivity().showToast(R.string.error_not_supported)
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            destroyer.destroy()
        }
    }

    object OpenImportExport : BaseActivityResultContract<IntentBuilder, Boolean>(::IntentBuilder) {

        private const val EXTRA_CATEGORIES_CHANGED = "categories_changed"

        override fun parseResult(resultCode: Int, intent: Intent?) =
            intent?.getBooleanExtra(EXTRA_CATEGORIES_CHANGED, false) ?: false

        fun createResult(categoriesChanged: Boolean) =
            createIntent {
                putExtra(EXTRA_CATEGORIES_CHANGED, categoriesChanged)
            }
    }

    class IntentBuilder : BaseIntentBuilder(ImportExportActivity::class.java)
}
