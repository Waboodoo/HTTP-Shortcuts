package ch.rmy.android.http_shortcuts.activities.widget

import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

data class WidgetSettingsViewState(
    val showLabel: Boolean,
    val labelColor: Int,
    val shortcutName: String,
    val shortcutIcon: ShortcutIcon,
) {
    val labelColorFormatted
        get() = String.format("#%06x", labelColor and 0xffffff)
}
