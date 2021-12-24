package ch.rmy.android.http_shortcuts.activities.settings

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.preference.Preference
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.showSnackbar
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.activities.settings.globalcode.GlobalScriptingActivity
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.http.CookieManager
import ch.rmy.android.http_shortcuts.logging.Logging
import ch.rmy.android.http_shortcuts.utils.DarkThemeHelper

class SettingsActivity : BaseActivity() {

    private val viewModel: SettingsViewModel by bindViewModel()

    override fun onCreate() {
        setContentView(R.layout.activity_settings)
        setTitle(R.string.title_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_view, SettingsFragment())
            .commit()
    }

    class SettingsFragment : BaseSettingsFragment() {

        private val viewModel: SettingsViewModel
            get() = (activity as SettingsActivity).viewModel

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

            initPreference("global_scripting") {
                openGlobalScriptingEditor()
            }

            if (Logging.supportsCrashReporting) {
                initListPreference("crash_reporting") { newValue ->
                    if (newValue == "false") {
                        Logging.disableCrashReporting(requireContext())
                    }
                }
            } else {
                findPreference<Preference>("privacy")!!.isVisible = false
            }

            initPreference("clear_cookies") {
                showClearCookieDialog()
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                initPreference("allow_overlay") {
                    try {
                        startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
                    } catch (e: ActivityNotFoundException) {
                        showSnackbar(R.string.error_not_supported)
                    }
                }
            } else {
                findPreference<Preference>("allow_overlay")!!.isVisible = false
            }
        }

        private fun restartToApplyThemeChanges() {
            val returnIntent = Intent().apply {
                putExtra(EXTRA_THEME_CHANGED, true)
            }
            requireActivity().apply {
                setResult(Activity.RESULT_OK, returnIntent)
                finish()
                overridePendingTransition(0, 0)
            }
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
                .subscribe(
                    {
                        val returnIntent = Intent().apply {
                            putExtra(EXTRA_APP_LOCKED, true)
                        }
                        requireActivity().apply {
                            setResult(Activity.RESULT_OK, returnIntent)
                            finish()
                        }
                    },
                    { e ->
                        activity?.showSnackbar(R.string.error_generic, long = true)
                        logException(e)
                    },
                )
                .attachTo(destroyer)
        }

        private fun openGlobalScriptingEditor() {
            GlobalScriptingActivity.IntentBuilder()
                .startActivity(this)
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

        override fun onDestroy() {
            super.onDestroy()
            destroyer.destroy()
        }
    }

    class IntentBuilder : BaseIntentBuilder(SettingsActivity::class.java)

    companion object {

        const val EXTRA_THEME_CHANGED = "theme_changed"
        const val EXTRA_APP_LOCKED = "app_locked"
    }
}
