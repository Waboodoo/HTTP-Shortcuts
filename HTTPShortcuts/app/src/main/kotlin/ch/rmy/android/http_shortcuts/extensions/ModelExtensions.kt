package ch.rmy.android.http_shortcuts.extensions

import ch.rmy.android.http_shortcuts.data.dtos.LauncherShortcut
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.models.Shortcut

val Shortcut.type: ShortcutExecutionType
    get() = ShortcutExecutionType.get(executionType!!)

fun Shortcut.toLauncherShortcut() =
    LauncherShortcut(
        id = id,
        name = name,
        icon = icon,
    )
