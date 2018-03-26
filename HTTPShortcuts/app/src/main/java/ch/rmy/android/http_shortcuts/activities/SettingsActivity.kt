package ch.rmy.android.http_shortcuts.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.NameNotFoundException
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.preference.Preference.OnPreferenceChangeListener
import android.preference.Preference.OnPreferenceClickListener
import android.preference.PreferenceFragment
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.dialogs.ChangeLogDialog
import ch.rmy.android.http_shortcuts.dialogs.HelpDialogBuilder
import ch.rmy.android.http_shortcuts.dialogs.MenuDialogBuilder
import ch.rmy.android.http_shortcuts.import_export.ExportTask
import ch.rmy.android.http_shortcuts.import_export.ImportTask
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.utils.CrashReporting
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import ch.rmy.android.http_shortcuts.utils.Settings
import ch.rmy.android.http_shortcuts.utils.showIfPossible
import ch.rmy.android.http_shortcuts.utils.showToast
import com.afollestad.materialdialogs.MaterialDialog
import com.nononsenseapps.filepicker.FilePickerActivity
import java.io.File

class SettingsActivity : BaseActivity() {

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        fragmentManager.beginTransaction().replace(R.id.settings_view, SettingsFragment()).commit()
    }

    class SettingsFragment : PreferenceFragment() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            addPreferencesFromResource(R.xml.preferences)

            initListPreference("click_behavior")

            initListPreference("theme") {
                val returnIntent = Intent()
                returnIntent.putExtra(EXTRA_THEME_CHANGED, true)
                activity.setResult(Activity.RESULT_OK, returnIntent)
                activity.finish()
                activity.overridePendingTransition(0, 0)
            }

            initPreference("export") {
                showExportOptions()
            }

            initPreference("import") {
                showImportOptions()
            }

            initPreference("privacy_policy") {
                HelpDialogBuilder(activity)
                        .title(R.string.title_privacy_policy)
                        .message(R.string.privacy_policy)
                        .build()
                        .show()
                        .attachTo((activity as BaseActivity).destroyer)
            }

            initListPreference("crash_reporting") { newValue ->
                CrashReporting.enabled = newValue != "false"
            }

            initPreference("changelog") {
                ChangeLogDialog(activity, false).show()
            }.let {
                it.summary = getString(R.string.settings_changelog_summary, try {
                    activity.packageManager.getPackageInfo(activity.packageName, 0).versionName
                } catch (e: NameNotFoundException) {
                    "???"
                })
            }

            initPreference("mail") {
                contactDeveloper()
            }

            initPreference("play_store") {
                openPlayStore()
            }

            initPreference("github") {
                gotoGithub()
            }

            initPreference("translate") {
                helpTranslate()
            }

            initPreference("licenses") {
                showLicenses()
            }
        }

        private fun initPreference(key: String, action: () -> Unit = {}): Preference {
            val preference = findPreference(key)
            preference.onPreferenceClickListener = OnPreferenceClickListener {
                action()
                true
            }
            return preference
        }

        private fun initListPreference(key: String, action: (newValue: Any) -> Unit = {}): ListPreference {
            val preference = findPreference(key) as ListPreference
            preference.onPreferenceChangeListener = OnPreferenceChangeListener { _, newValue ->
                updateSummary(preference, newValue)
                action(newValue)
                true
            }
            updateSummary(preference, null)
            return preference
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
                    .item(R.string.button_export_to_filesystem, this::showExportInstructions)
                    .item(R.string.button_export_send_to, this::sendExport)
                    .showIfPossible()
        }

        private fun showExportInstructions() {
            MaterialDialog.Builder(activity)
                    .positiveText(R.string.button_ok)
                    .negativeText(R.string.button_cancel)
                    .content(R.string.export_instructions)
                    .onPositive { _, _ -> openFilePickerForExport() }
                    .showIfPossible()
        }

        private fun sendExport() {
            Controller().use { controller ->
                val base = controller.exportBase()
                val data = GsonUtil.exportData(base)
                val sharingIntent = Intent(android.content.Intent.ACTION_SEND)
                sharingIntent.type = IMPORT_EXPORT_FILE_TYPE
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, data)
                startActivity(Intent.createChooser(sharingIntent, getString(R.string.title_export)))
            }
        }

        private fun showImportOptions() {
            MenuDialogBuilder(activity)
                    .title(R.string.title_import)
                    .item(R.string.button_import_from_filesystem, this::showImportInstructions)
                    .item(R.string.button_import_from_general, this::openGeneralPickerForImport)
                    .showIfPossible()
        }

        private fun showImportInstructions() {
            MaterialDialog.Builder(activity)
                    .positiveText(R.string.button_ok)
                    .negativeText(R.string.button_cancel)
                    .content(R.string.import_instructions)
                    .onPositive { _, _ -> openLocalFilePickerForImport() }
                    .showIfPossible()
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
            val pickerIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Intent(Intent.ACTION_OPEN_DOCUMENT)
            } else {
                Intent(Intent.ACTION_GET_CONTENT)
            }
            pickerIntent.type = "*/*"
            pickerIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            pickerIntent.addCategory(Intent.CATEGORY_OPENABLE)
            try {
                startActivityForResult(pickerIntent, REQUEST_IMPORT_FROM_DOCUMENTS)
            } catch (e: ActivityNotFoundException) {
                activity.showToast(R.string.error_not_supported)
            }
        }

        private fun contactDeveloper() {
            sendMail(CONTACT_SUBJECT, CONTACT_TEXT, getString(R.string.settings_mail))
        }

        private fun sendMail(subject: String, text: String, title: String) {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$DEVELOPER_EMAIL"))
            val recipients = arrayOf(DEVELOPER_EMAIL)
            intent.putExtra(Intent.EXTRA_EMAIL, recipients)
            intent.putExtra(Intent.EXTRA_SUBJECT, subject)
            intent.putExtra(Intent.EXTRA_TEXT, text)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                startActivity(Intent.createChooser(intent, title))
            } catch (e: ActivityNotFoundException) {
                activity.showToast(R.string.error_not_supported)
            }
        }

        private fun openPlayStore() {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_URL))
                startActivity(browserIntent)
            } catch (e: ActivityNotFoundException) {
                activity.showToast(R.string.error_not_supported)
            }
        }

        private fun gotoGithub() {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL))
                startActivity(browserIntent)
            } catch (e: ActivityNotFoundException) {
                activity.showToast(R.string.error_not_supported)
            }
        }

        private fun helpTranslate() {
            sendMail(TRANSLATE_SUBJECT, TRANSLATE_TEXT, getString(R.string.settings_help_translate))
        }

        private fun showLicenses() {
            val intent = LicensesActivity.IntentBuilder(activity)
                    .build()
            startActivity(intent)
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
            if (resultCode != RESULT_OK || intent == null) {
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

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, SettingsActivity::class.java)

    companion object {

        const val EXTRA_THEME_CHANGED = "theme_changed"

        private const val CONTACT_SUBJECT = "HTTP Shortcuts"
        private const val CONTACT_TEXT = "Hey Roland,\n\n"
        private const val TRANSLATE_SUBJECT = "Translate HTTP Shortcuts"
        private const val TRANSLATE_TEXT = "Hey Roland,\n\nI would like to help translate your app into [LANGUAGE]. Please give me access to the translation tool."
        private const val DEVELOPER_EMAIL = "android@rmy.ch"
        private const val PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=ch.rmy.android.http_shortcuts"
        private const val GITHUB_URL = "https://github.com/Waboodoo/HTTP-Shortcuts"

        private const val REQUEST_PICK_DIR_FOR_EXPORT = 1
        private const val REQUEST_PICK_FILE_FOR_IMPORT = 2
        private const val REQUEST_IMPORT_FROM_DOCUMENTS = 3

        private const val IMPORT_EXPORT_FILE_TYPE = "text/plain"
    }

}
