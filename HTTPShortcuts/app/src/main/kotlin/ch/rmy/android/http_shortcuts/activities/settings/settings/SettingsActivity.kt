package ch.rmy.android.http_shortcuts.activities.settings.settings

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.core.net.toUri
import androidx.preference.Preference
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.createIntent
import ch.rmy.android.framework.extensions.initialize
import ch.rmy.android.framework.extensions.observe
import ch.rmy.android.framework.extensions.showSnackbar
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.framework.ui.BaseActivityResultContract
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.activities.settings.BaseSettingsFragment
import ch.rmy.android.http_shortcuts.activities.settings.globalcode.GlobalScriptingActivity
import ch.rmy.android.http_shortcuts.logging.Logging
import ch.rmy.android.http_shortcuts.utils.DarkThemeHelper

class SettingsActivity : BaseActivity() {

    private val viewModel: SettingsViewModel by bindViewModel()

    override fun onCreated(savedState: Bundle?) {
        viewModel.initialize()
        initViews(savedState == null)
        initViewModelBindings()
    }

    private fun initViews(firstInit: Boolean) {
        setContentView(R.layout.activity_settings)
        setTitle(R.string.title_settings)
        if (firstInit) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_view, SettingsFragment())
                .commit()
        }
    }

    private fun initViewModelBindings() {
        viewModel.viewState.observe(this) { viewState ->
            setDialogState(viewState.dialogState, viewModel)
        }
        viewModel.events.observe(this, ::handleEvent)
    }

    class SettingsFragment : BaseSettingsFragment() {

        private val viewModel: SettingsViewModel
            get() = (activity as SettingsActivity).viewModel

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)

            initListPreference("language") { newLanguage ->
                viewModel.onLanguageChanged(newLanguage as String)
            }

            initListPreference("click_behavior")

            initListPreference("theme") {
                restartToApplyThemeChanges()
            }

            initListPreference("dark_theme") { newSetting ->
                DarkThemeHelper.applyDarkThemeSettings(newSetting as String)
                restartToApplyThemeChanges()
            }

            initPreference("title") {
                viewModel.onChangeTitleButtonClicked()
            }

            initPreference("lock_settings") {
                viewModel.onLockAppButtonClicked()
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
                viewModel.onClearCookiesButtonClicked()
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                initPreference("allow_overlay") {
                    try {
                        startActivity(
                            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                                .setData("package:${requireContext().packageName}".toUri())
                        )
                    } catch (e: ActivityNotFoundException) {
                        showSnackbar(R.string.error_not_supported)
                    }
                }
            } else {
                findPreference<Preference>("allow_overlay")!!.isVisible = false
            }

            if (Build.MANUFACTURER?.equals("xiaomi", ignoreCase = true) == true) {
                initPreference("allow_overlay_xiaomi") {
                    try {
                        startActivity(
                            Intent("miui.intent.action.APP_PERM_EDITOR")
                                .setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity")
                                .putExtra("extra_pkgname", requireContext().packageName)
                        )
                    } catch (e: ActivityNotFoundException) {
                        showSnackbar(R.string.error_not_supported)
                    }
                }
            } else {
                findPreference<Preference>("allow_overlay_xiaomi")!!.isVisible = false
            }
        }

        private fun restartToApplyThemeChanges() {
            requireActivity().apply {
                setResult(Activity.RESULT_OK, OpenSettings.createResult(themeChanged = true))
                finish()
                overridePendingTransition(0, 0)
            }
        }

        private fun openGlobalScriptingEditor() {
            GlobalScriptingActivity.IntentBuilder()
                .startActivity(this)
        }

        override fun onDestroy() {
            super.onDestroy()
            destroyer.destroy()
        }
    }

    object OpenSettings : BaseActivityResultContract<IntentBuilder, OpenSettings.Result>(::IntentBuilder) {

        private const val EXTRA_THEME_CHANGED = "theme_changed"
        private const val EXTRA_APP_LOCKED = "app_locked"

        override fun parseResult(resultCode: Int, intent: Intent?): Result =
            Result(
                themeChanged = intent?.getBooleanExtra(EXTRA_THEME_CHANGED, false) ?: false,
                appLocked = intent?.getBooleanExtra(EXTRA_APP_LOCKED, false) ?: false,
            )

        fun createResult(themeChanged: Boolean = false, appLocked: Boolean = false) =
            createIntent {
                putExtra(EXTRA_THEME_CHANGED, themeChanged)
                putExtra(EXTRA_APP_LOCKED, appLocked)
            }

        data class Result(val themeChanged: Boolean, val appLocked: Boolean)
    }

    class IntentBuilder : BaseIntentBuilder(SettingsActivity::class.java)
}
