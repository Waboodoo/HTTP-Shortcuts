package ch.rmy.android.http_shortcuts.activities.icons

import android.content.Intent
import androidx.compose.runtime.Composable
import ch.rmy.android.framework.extensions.createIntent
import ch.rmy.android.framework.ui.BaseActivityResultContract
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import ch.rmy.android.http_shortcuts.components.ScreenScope
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

class IconPickerActivity : BaseComposeActivity() {

    @Composable
    override fun ScreenScope.Content() {
        IconPickerScreen()
    }

    object PickIcon : BaseActivityResultContract<IntentBuilder, ShortcutIcon?>(::IntentBuilder) {
        override fun parseResult(resultCode: Int, intent: Intent?): ShortcutIcon? =
            intent?.getStringExtra(EXTRA_ICON)?.let(ShortcutIcon::fromName)

        fun createResult(icon: ShortcutIcon) =
            createIntent {
                putExtra(EXTRA_ICON, icon.toString())
            }

        private const val EXTRA_ICON = "icon"
    }

    class IntentBuilder : BaseIntentBuilder(IconPickerActivity::class)
}
