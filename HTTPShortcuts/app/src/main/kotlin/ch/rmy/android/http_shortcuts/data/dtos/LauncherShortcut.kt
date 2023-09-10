package ch.rmy.android.http_shortcuts.data.dtos

import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

data class LauncherShortcut(
    val id: String,
    val name: String,
    val icon: ShortcutIcon,
    val isTextShareTarget: Boolean,
    val isFileShareTarget: Boolean,
)
