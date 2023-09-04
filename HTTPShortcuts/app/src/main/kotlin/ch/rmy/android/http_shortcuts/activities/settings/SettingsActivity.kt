package ch.rmy.android.http_shortcuts.activities.settings

import android.content.Intent
import androidx.compose.runtime.Composable
import ch.rmy.android.framework.extensions.createIntent
import ch.rmy.android.framework.ui.BaseActivityResultContract
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : BaseComposeActivity() {

    @Composable
    override fun Content() {
        SettingsScreen()
    }

    object OpenSettings : BaseActivityResultContract<IntentBuilder, OpenSettings.Result>(SettingsActivity::IntentBuilder) {

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
