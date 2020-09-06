package ch.rmy.android.http_shortcuts.activities.settings

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.NameNotFoundException
import android.net.Uri
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.activities.misc.AcknowledgmentActivity
import ch.rmy.android.http_shortcuts.dialogs.ChangeLogDialog
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.dialogs.SpecialWarnings
import ch.rmy.android.http_shortcuts.extensions.applyTheme
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.bindViewModel
import ch.rmy.android.http_shortcuts.extensions.isWebUrl
import ch.rmy.android.http_shortcuts.extensions.logException
import ch.rmy.android.http_shortcuts.extensions.showMessageDialog
import ch.rmy.android.http_shortcuts.extensions.showSnackbar
import ch.rmy.android.http_shortcuts.extensions.showToast
import ch.rmy.android.http_shortcuts.extensions.startActivity
import ch.rmy.android.http_shortcuts.http.CookieManager
import ch.rmy.android.http_shortcuts.import_export.ExportUI
import ch.rmy.android.http_shortcuts.import_export.ImportException
import ch.rmy.android.http_shortcuts.import_export.Importer
import ch.rmy.android.http_shortcuts.logging.Logging
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.utils.DarkThemeHelper
import ch.rmy.android.http_shortcuts.utils.Destroyer
import ch.rmy.android.http_shortcuts.utils.ExternalURLs
import ch.rmy.android.http_shortcuts.utils.FilePickerUtil
import ch.rmy.android.http_shortcuts.utils.Settings
import io.reactivex.android.schedulers.AndroidSchedulers


class SettingsActivity : BaseActivity() {

    private val viewModel: SettingsViewModel by bindViewModel()

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setTitle(R.string.title_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_view, SettingsFragment())
            .commit()
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        private val destroyer = Destroyer()

        private val viewModel: SettingsViewModel
            get() = (activity as SettingsActivity).viewModel

        private val exportUI by lazy {
            ExportUI(requireActivity())
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)

            initListPreference("language") {
                restartToApplyThemeChanges()
            }

            initListPreference("click_behavior")

            initListPreference("theme") {
                restartToApplyThemeChanges()
            }

            initListPreference("dark_theme") { newSetting ->
                DarkThemeHelper.applyDarkThemeSettings(newSetting as String)
                restartToApplyThemeChanges()
            }

            initPreference("lock_settings") {
                showAppLockDialog()
            }

            initPreference("export") {
                exportUI.showExportOptions { intent ->
                    intent.startActivity(this, REQUEST_EXPORT_TO_DOCUMENTS)
                }
            }

            initPreference("import") {
                showImportOptions()
            }

            initPreference("privacy_policy") {
                openURL(ExternalURLs.PRIVACY_POLICY)
            }

            if (Logging.supportsCrashReporting) {
                initListPreference("crash_reporting") { newValue ->
                    if (newValue == "false") {
                        Logging.disableCrashReporting()
                    }
                }
            } else {
                findPreference<Preference>("crash_reporting")!!.isVisible = false
            }

            initPreference("clear_cookies") {
                showClearCookieDialog()
            }

            initPreference("changelog") {
                ChangeLogDialog(requireContext(), false)
                    .show()
                    .subscribe({}, {
                        showSnackbar(R.string.error_generic, long = true)
                    })
                    .attachTo(destroyer)
            }
                .summary = getString(R.string.settings_changelog_summary, versionName)

            initPreference("mail") {
                contactDeveloper()
            }

            initPreference("faq") {
                openURL(ExternalURLs.FAQ_PAGE)
            }

            initPreference("play_store") {
                openURL(ExternalURLs.PLAY_STORE)
            }

            initPreference("github") {
                openURL(ExternalURLs.GITHUB)
            }

            initPreference("translate") {
                openURL(ExternalURLs.TRANSLATION)
            }

            initPreference("acknowledgments") {
                showAcknowledgments()
            }
        }

        private fun restartToApplyThemeChanges() {
            val returnIntent = Intent().apply {
                putExtra(EXTRA_THEME_CHANGED, true)
            }
            requireActivity().setResult(Activity.RESULT_OK, returnIntent)
            requireActivity().finish()
            requireActivity().overridePendingTransition(0, 0)
        }

        private val versionName: String
            get() = try {
                requireContext().packageManager.getPackageInfo(requireContext().packageName, 0).versionName
            } catch (e: NameNotFoundException) {
                "???"
            }

        private fun initPreference(key: String, action: () -> Unit = {}): Preference {
            val preference = findPreference<Preference>(key)!!
            preference.applyTheme()
            preference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                action()
                true
            }
            return preference
        }

        private fun initListPreference(key: String, action: (newValue: Any) -> Unit = {}): ListPreference {
            val preference = findPreference<ListPreference>(key)!!
            preference.applyTheme()
            preference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                if (isAdded) {
                    updateSummary(preference, newValue)
                    action(newValue)
                }
                true
            }
            updateSummary(preference, null)
            return preference
        }

        private fun updateSummary(preference: ListPreference, value: Any?) {
            val index = preference.findIndexOfValue((value ?: preference.value) as String?)
                .takeUnless { it == -1 }
            preference.summary = preference.entries[index ?: 0]
        }

        private fun showAppLockDialog() {
            DialogBuilder(requireContext())
                .title(R.string.dialog_title_lock_app)
                .message(R.string.dialog_text_lock_app)
                .positive(R.string.button_lock_app)
                .textInput(allowEmpty = false, maxLength = 50) { input ->
                    lockApp(input)
                }
                .negative(R.string.dialog_cancel)
                .showIfPossible()
        }

        private fun lockApp(password: String) {
            viewModel.setAppLock(password)
                .subscribe({
                    val returnIntent = Intent().apply {
                        putExtra(EXTRA_APP_LOCKED, true)
                    }
                    requireActivity().setResult(Activity.RESULT_OK, returnIntent)
                    requireActivity().finish()
                },
                    { e ->
                        activity?.showSnackbar(R.string.error_generic, long = true)
                        logException(e)
                    })
                .attachTo(destroyer)
        }

        private fun showImportOptions() {
            DialogBuilder(requireContext())
                .title(R.string.title_import)
                .item(R.string.button_import_from_general, action = ::openGeneralPickerForImport)
                .item(R.string.button_import_from_url, action = ::openImportUrlDialog)
                .showIfPossible()
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
                    prefill = Settings(requireContext()).importUrl,
                    allowEmpty = false,
                    callback = ::startImportFromURL
                )
                .showIfPossible()
        }

        private fun startImportFromURL(url: String) {
            val uri = Uri.parse(url)
            persistImportUrl(url)
            if (uri.isWebUrl) {
                startImport(uri)
            } else {
                showMessageDialog(getString(R.string.import_failed_with_reason, getString(R.string.error_can_only_import_from_http_url)))
            }
        }

        private fun showClearCookieDialog() {
            DialogBuilder(requireContext())
                .message(R.string.confirm_clear_cookies_message)
                .positive(R.string.dialog_delete) {
                    CookieManager.clearCookies(requireContext())
                    activity?.showSnackbar(R.string.message_cookies_cleared)
                }
                .negative(R.string.dialog_cancel)
                .showIfPossible()
        }

        private fun contactDeveloper() {
            ContactActivity.IntentBuilder(requireContext())
                .build()
                .startActivity(this)
        }

        private fun openURL(url: String) {
            try {
                Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    .startActivity(this)
            } catch (e: ActivityNotFoundException) {
                requireActivity().showToast(R.string.error_not_supported)
            }
        }

        private fun showAcknowledgments() {
            AcknowledgmentActivity.IntentBuilder(requireContext())
                .build()
                .startActivity(this)
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
            if (resultCode != RESULT_OK || intent == null) {
                return
            }
            when (requestCode) {
                REQUEST_EXPORT_TO_DOCUMENTS -> {
                    exportUI.startExport(intent.data ?: return)
                }
                REQUEST_IMPORT_FROM_DOCUMENTS -> {
                    startImport(intent.data ?: return)
                }
            }
        }

        private fun persistImportUrl(url: String) {
            Settings(requireContext()).importUrl = url
        }


        private fun startImport(uri: Uri) {
            // TODO: Replace progress dialog with something better
            val progressDialog = ProgressDialog(activity).apply {
                setMessage(getString(R.string.import_in_progress))
                setCanceledOnTouchOutside(false)
            }
            Importer(requireContext().applicationContext)
                .import(uri)
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
                    showSnackbar(requireContext().resources.getQuantityString(
                        R.plurals.shortcut_import_success,
                        status.importedShortcuts,
                        status.importedShortcuts
                    ))
                    requireActivity().setResult(Activity.RESULT_OK, Intent().apply {
                        putExtra(EXTRA_CATEGORIES_CHANGED, true)
                    })
                }, { e ->
                    if (e !is ImportException) {
                        logException(e)
                    }
                    showMessageDialog(getString(R.string.import_failed_with_reason, e.message))
                })
                .attachTo(destroyer)
        }

        private fun showSnackbar(message: CharSequence) {
            activity?.showSnackbar(message)
        }

        override fun onDestroy() {
            super.onDestroy()
            destroyer.destroy()
        }

    }

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, SettingsActivity::class.java)

    companion object {

        const val EXTRA_THEME_CHANGED = "theme_changed"
        const val EXTRA_APP_LOCKED = "app_locked"
        const val EXTRA_CATEGORIES_CHANGED = "categories_changed"

        private const val REQUEST_EXPORT_TO_DOCUMENTS = 2
        private const val REQUEST_IMPORT_FROM_DOCUMENTS = 3

    }

}
