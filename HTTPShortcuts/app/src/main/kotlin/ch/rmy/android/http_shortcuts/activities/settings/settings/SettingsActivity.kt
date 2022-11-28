package ch.rmy.android.http_shortcuts.activities.settings.settings

import android.app.Activity
import android.app.StatusBarManager
import android.app.StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ADDED
import android.app.StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.preference.Preference
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.collectEventsWhileActive
import ch.rmy.android.framework.extensions.collectViewStateWhileActive
import ch.rmy.android.framework.extensions.createIntent
import ch.rmy.android.framework.extensions.initialize
import ch.rmy.android.framework.extensions.isDarkThemeEnabled
import ch.rmy.android.framework.extensions.showSnackbar
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.framework.ui.BaseActivityResultContract
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.activities.settings.BaseSettingsFragment
import ch.rmy.android.http_shortcuts.activities.settings.globalcode.GlobalScriptingActivity
import ch.rmy.android.http_shortcuts.logging.Logging
import ch.rmy.android.http_shortcuts.utils.DarkThemeHelper

class SettingsActivity : BaseActivity() {

    internal val viewModel: SettingsViewModel by bindViewModel()

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
        collectViewStateWhileActive(viewModel) { viewState ->
            setDialogState(viewState.dialogState, viewModel)
        }
        collectEventsWhileActive(viewModel, ::handleEvent)
    }

    override fun handleEvent(event: ViewModelEvent) {
        when (event) {
            is SettingsEvent.AddQuickSettingsTile -> addQuickSettingsTile()
            else -> super.handleEvent(event)
        }
    }

    private fun addQuickSettingsTile() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getSystemService<StatusBarManager>()!!.requestAddTileService(
                ComponentName.createRelative(context, "ch.rmy.android.http_shortcuts.tiles.QuickTileService"),
                getString(R.string.action_quick_settings_tile_trigger),
                Icon.createWithResource(context, R.drawable.ic_quick_settings_tile),
                mainExecutor,
            ) { result ->
                if (result == TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED || result == TILE_ADD_REQUEST_RESULT_TILE_ADDED) {
                    showSnackbar(R.string.message_quick_settings_tile_added)
                }
            }
        }
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
                .isEnabled = !requireContext().isDarkThemeEnabled()

            initListPreference("dark_theme") { newSetting ->
                DarkThemeHelper.applyDarkThemeSettings(newSetting as String)
                restartToApplyThemeChanges()
            }

            initPreference("title") {
                viewModel.onChangeTitleButtonClicked()
            }

            initPreference("quick_settings_tile", isVisible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                viewModel.onAddQuickSettingsTileButtonClicked()
            }

            initPreference("lock_settings") {
                viewModel.onLockAppButtonClicked()
            }

            initPreference("global_scripting") {
                openGlobalScriptingEditor()
            }

            findPreference<Preference>("privacy")!!.isVisible = Logging.supportsCrashReporting
            initListPreference("crash_reporting") { newValue ->
                if (newValue == "false") {
                    Logging.disableCrashReporting(requireContext())
                }
            }

            initPreference("clear_cookies") {
                viewModel.onClearCookiesButtonClicked()
            }

            initPreference("allow_overlay", isVisible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    try {
                        startActivity(
                            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                                .setData("package:${requireContext().packageName}".toUri())
                        )
                    } catch (e: ActivityNotFoundException) {
                        showSnackbar(R.string.error_not_supported)
                    }
                }
            }

            initPreference("allow_overlay_xiaomi", isVisible = isXiaomiDevice()) {
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
        }

        private fun isXiaomiDevice() =
            Build.MANUFACTURER?.equals("xiaomi", ignoreCase = true) == true

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

    class IntentBuilder : BaseIntentBuilder(SettingsActivity::class)
}
