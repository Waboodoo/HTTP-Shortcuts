package ch.rmy.android.http_shortcuts.activities.widget

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

@Stable
data class WidgetSettingsViewState(
    val showLabel: Boolean,
    val showIcon: Boolean,
    val labelColor: Int,
    val iconScale: Float,
    val shortcutName: String,
    val shortcutIcon: ShortcutIcon,
    val colorDialogVisible: Boolean = false,
) {
    val labelColorFormatted
        get() = String.format("#%06x", labelColor and 0xffffff)
}
