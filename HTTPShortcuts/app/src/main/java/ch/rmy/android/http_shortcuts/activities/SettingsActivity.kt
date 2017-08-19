package ch.rmy.android.http_shortcuts.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager.NameNotFoundException
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference.OnPreferenceChangeListener
import android.preference.Preference.OnPreferenceClickListener
import android.preference.PreferenceFragment
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.dialogs.ChangeLogDialog
import ch.rmy.android.http_shortcuts.import_export.ExportTask
import ch.rmy.android.http_shortcuts.import_export.ImportTask
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import ch.rmy.android.http_shortcuts.utils.MenuDialogBuilder
import ch.rmy.android.http_shortcuts.utils.Settings
import com.afollestad.materialdialogs.MaterialDialog
import com.nononsenseapps.filepicker.FilePickerActivity
import java.io.File

class SettingsActivity : BaseActivity() {

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val settingsFragment = SettingsFragment()
        fragmentManager.beginTransaction().replace(R.id.settings_view, settingsFragment).commit()
    }

    class SettingsFragment : PreferenceFragment() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            addPreferencesFromResource(R.xml.preferences)

            val clickBehaviorPreference = findPreference("click_behavior") as ListPreference
            clickBehaviorPreference.onPreferenceChangeListener = OnPreferenceChangeListener { _, newValue ->
                updateSummary(clickBehaviorPreference, newValue)
                true
            }
            updateSummary(clickBehaviorPreference, null)

            val themePreference = findPreference("theme") as ListPreference
            themePreference.onPreferenceChangeListener = OnPreferenceChangeListener { _, newValue ->
                updateSummary(themePreference, newValue)
                val returnIntent = Intent()
                returnIntent.putExtra(EXTRA_THEME_CHANGED, true)
                activity.setResult(Activity.RESULT_OK, returnIntent)
                activity.finish()
                activity.overridePendingTransition(0, 0)
                true
            }
            updateSummary(themePreference, null)

            val exportPreference = findPreference("export")
            exportPreference.onPreferenceClickListener = OnPreferenceClickListener {
                showExportOptions()
                true
            }

            val importPreference = findPreference("import")
            importPreference.onPreferenceClickListener = OnPreferenceClickListener {
                showImportOptions()
                true
            }

            val versionPreference = findPreference("version")
            try {
                versionPreference.summary = activity.packageManager.getPackageInfo(activity.packageName, 0).versionName
            } catch (e: NameNotFoundException) {
                versionPreference.summary = "???"
            }

            versionPreference.onPreferenceClickListener = OnPreferenceClickListener {
                ChangeLogDialog(activity, false).show()
                true
            }

            val mailPreference = findPreference("mail")
            mailPreference.onPreferenceClickListener = OnPreferenceClickListener {
                sendMail()
                true
            }

            val playStorePreference = findPreference("play_store")
            playStorePreference.onPreferenceClickListener = OnPreferenceClickListener {
                openPlayStore()
                true
            }

            val githubPreference = findPreference("github")
            githubPreference.onPreferenceClickListener = OnPreferenceClickListener {
                gotoGithub()
                true
            }

            val licensesPreference = findPreference("licenses")
            licensesPreference.onPreferenceClickListener = OnPreferenceClickListener {
                showLicenses()
                true
            }
        }

        private fun updateSummary(preference: ListPreference, value: Any?) {
            var index = preference.findIndexOfValue((value ?: preference.value) as String?)
            if (index == -1) {
                index = 0
            }
            preference.summary = preference.entries[index]
        }

        private fun showExportOptions() {
            MenuDialogBuilder(activity)
                    .title(R.string.title_export)
                    .item(R.string.button_export_to_filesystem, {
                        showExportInstructions()
                    })
                    .item(R.string.button_export_send_to, {
                        sendExport()
                    }).show()
        }

        private fun showExportInstructions() {
            MaterialDialog.Builder(activity)
                    .positiveText(R.string.button_ok)
                    .negativeText(R.string.button_cancel)
                    .content(R.string.export_instructions)
                    .onPositive { _, _ -> openFilePickerForExport() }
                    .show()
        }

        private fun sendExport() {
            var controller: Controller? = null
            try {
                controller = Controller()
                val base = controller.exportBase()
                val data = GsonUtil.exportData(base)
                val sharingIntent = Intent(android.content.Intent.ACTION_SEND)
                sharingIntent.type = IMPORT_EXPORT_FILE_TYPE
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, data)
                startActivity(Intent.createChooser(sharingIntent, getString(R.string.title_export)))
            } finally {
                controller?.destroy()
            }
        }

        private fun showImportOptions() {
            MenuDialogBuilder(activity)
                    .title(R.string.title_import)
                    .item(R.string.button_import_from_filesystem, {
                        showImportInstructions()
                    })
                    .item(R.string.button_import_from_general, {
                        openGeneralPickerForImport()
                    }).show()
        }

        private fun showImportInstructions() {
            MaterialDialog.Builder(activity)
                    .positiveText(R.string.button_ok)
                    .negativeText(R.string.button_cancel)
                    .content(R.string.import_instructions)
                    .onPositive { _, _ -> openLocalFilePickerForImport() }
                    .show()
        }

        private fun openFilePickerForExport() {
            val intent = Intent(activity, FilePickerActivity::class.java)
            intent.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)
            intent.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true)
            intent.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR)
            intent.putExtra(FilePickerActivity.EXTRA_START_PATH, Settings(activity).importExportDirectory)
            startActivityForResult(intent, REQUEST_PICK_DIR_FOR_EXPORT)
        }

        private fun openLocalFilePickerForImport() {
            val intent = Intent(activity, FilePickerActivity::class.java)
            intent.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)
            intent.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false)
            intent.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE)
            intent.putExtra(FilePickerActivity.EXTRA_START_PATH, Settings(activity).importExportDirectory)
            startActivityForResult(intent, REQUEST_PICK_FILE_FOR_IMPORT)
        }

        private fun openGeneralPickerForImport() {
            val pickerIntent: Intent
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                pickerIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            } else {
                pickerIntent = Intent(Intent.ACTION_GET_CONTENT)
            }
            pickerIntent.type = IMPORT_EXPORT_FILE_TYPE
            pickerIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            pickerIntent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(pickerIntent, REQUEST_IMPORT_FROM_DOCUMENTS)
        }

        private fun sendMail() {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + DEVELOPER_EMAIL))
            val recipients = arrayOf(DEVELOPER_EMAIL)
            intent.putExtra(Intent.EXTRA_EMAIL, recipients)
            intent.putExtra(Intent.EXTRA_SUBJECT, CONTACT_SUBJECT)
            intent.putExtra(Intent.EXTRA_TEXT, CONTACT_TEXT)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(Intent.createChooser(intent, getString(R.string.settings_mail)))
        }

        private fun openPlayStore() {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_URL))
            startActivity(browserIntent)
        }

        private fun gotoGithub() {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL))
            startActivity(browserIntent)
        }

        private fun showLicenses() {
            val licensesIntent = Intent(activity, LicensesActivity::class.java)
            startActivity(licensesIntent)
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
            if (resultCode != Activity.RESULT_OK || intent == null) {
                return
            }
            when (requestCode) {
                REQUEST_PICK_DIR_FOR_EXPORT -> {
                    val uri = intent.data
                    val directoryPath = uri.path
                    persistPath(directoryPath)
                    startExport(directoryPath)
                }
                REQUEST_PICK_FILE_FOR_IMPORT -> {
                    val uri = intent.data
                    val filePath = uri.path
                    val directoryPath = File(filePath).parent
                    persistPath(directoryPath)
                    startImport(uri)
                }
                REQUEST_IMPORT_FROM_DOCUMENTS -> {
                    val uri = intent.data
                    startImport(uri)
                }
            }
        }

        private fun persistPath(path: String) {
            Settings(activity).importExportDirectory = path
        }

        private fun startExport(directoryPath: String) {
            val task = ExportTask(activity, view!!)
            task.execute(directoryPath)
        }

        private fun startImport(uri: Uri) {
            val task = ImportTask(activity, view!!)
            task.execute(uri)
        }

    }

    companion object {

        const val REQUEST_SETTINGS = 52
        const val EXTRA_THEME_CHANGED = "theme_changed"

        private const val CONTACT_SUBJECT = "HTTP Shortcuts"
        private const val CONTACT_TEXT = "Hey Roland,\n\n"
        private const val DEVELOPER_EMAIL = "android@rmy.ch"
        private const val PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=ch.rmy.android.http_shortcuts"
        private const val GITHUB_URL = "https://github.com/Waboodoo/HTTP-Shortcuts"

        private const val REQUEST_PICK_DIR_FOR_EXPORT = 1
        private const val REQUEST_PICK_FILE_FOR_IMPORT = 2
        private const val REQUEST_IMPORT_FROM_DOCUMENTS = 3

        private const val IMPORT_EXPORT_FILE_TYPE = "text/plain"
    }

}
