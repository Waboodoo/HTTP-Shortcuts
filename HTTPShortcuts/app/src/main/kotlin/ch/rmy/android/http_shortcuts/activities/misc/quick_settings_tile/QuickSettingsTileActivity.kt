package ch.rmy.android.http_shortcuts.activities.misc.quick_settings_tile

import androidx.compose.runtime.Composable
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QuickSettingsTileActivity : BaseComposeActivity() {

    override val initializeWithTheme: Boolean
        get() = false

    @Composable
    override fun Content() {
        QuickSettingsTileScreen()
    }

    class IntentBuilder : BaseIntentBuilder(QuickSettingsTileActivity::class)
}
