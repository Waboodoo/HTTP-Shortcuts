package ch.rmy.android.http_shortcuts.activities.widget

import android.app.Activity
import android.content.Intent
import androidx.compose.runtime.Composable
import ch.rmy.android.framework.extensions.createIntent
import ch.rmy.android.framework.ui.BaseActivityResultContract
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import ch.rmy.android.http_shortcuts.components.ScreenScope
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.dtos.LauncherShortcut
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

class WidgetSettingsActivity : BaseComposeActivity() {

    @Composable
    override fun ScreenScope.Content() {
        WidgetSettingsScreen(
            shortcutId = intent.getStringExtra(EXTRA_SHORTCUT_ID)!!,
            shortcutName = intent.getStringExtra(EXTRA_SHORTCUT_NAME)!!,
            shortcutIcon = ShortcutIcon.fromName(intent.getStringExtra(EXTRA_SHORTCUT_ICON)!!),
        )
    }

    object OpenWidgetSettings : BaseActivityResultContract<IntentBuilder, OpenWidgetSettings.Result?>(::IntentBuilder) {

        private const val EXTRA_SHOW_LABEL = "ch.rmy.android.http_shortcuts.activities.widget.WidgetSettingsActivity.show_label"
        private const val EXTRA_LABEL_COLOR = "ch.rmy.android.http_shortcuts.activities.widget.WidgetSettingsActivity.label_color"

        override fun parseResult(resultCode: Int, intent: Intent?): Result? =
            if (resultCode == Activity.RESULT_OK && intent != null) {
                Result(
                    shortcutId = intent.getStringExtra(EXTRA_SHORTCUT_ID)!!,
                    labelColor = intent.getStringExtra(EXTRA_LABEL_COLOR)!!,
                    showLabel = intent.getBooleanExtra(EXTRA_SHOW_LABEL, true),
                )
            } else null

        fun createResult(shortcutId: ShortcutId, labelColor: String, showLabel: Boolean): Intent =
            createIntent {
                putExtra(EXTRA_SHORTCUT_ID, shortcutId)
                putExtra(EXTRA_LABEL_COLOR, labelColor)
                putExtra(EXTRA_SHOW_LABEL, showLabel)
            }

        data class Result(
            val shortcutId: ShortcutId,
            val labelColor: String,
            val showLabel: Boolean,
        )
    }

    class IntentBuilder : BaseIntentBuilder(WidgetSettingsActivity::class) {

        fun shortcut(shortcut: LauncherShortcut) = also {
            intent.putExtra(EXTRA_SHORTCUT_ID, shortcut.id)
            intent.putExtra(EXTRA_SHORTCUT_NAME, shortcut.name)
            intent.putExtra(EXTRA_SHORTCUT_ICON, shortcut.icon.toString())
        }
    }

    companion object {
        private const val EXTRA_SHORTCUT_ID = "ch.rmy.android.http_shortcuts.activities.widget.WidgetSettingsActivity.shortcut_id"
        private const val EXTRA_SHORTCUT_NAME = "ch.rmy.android.http_shortcuts.activities.widget.WidgetSettingsActivity.shortcut_name"
        private const val EXTRA_SHORTCUT_ICON = "ch.rmy.android.http_shortcuts.activities.widget.WidgetSettingsActivity.shortcut_icon"
    }
}
