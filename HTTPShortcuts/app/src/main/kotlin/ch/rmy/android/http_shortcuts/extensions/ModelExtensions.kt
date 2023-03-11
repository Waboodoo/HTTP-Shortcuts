package ch.rmy.android.http_shortcuts.extensions

import android.content.Context
import ch.rmy.android.http_shortcuts.R
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

fun Shortcut.getSafeName(context: Context) =
    name.ifEmpty { context.getString(R.string.shortcut_safe_name) }

val Shortcut.isTemporaryShortcut
    get() = id == Shortcut.TEMPORARY_ID

fun Shortcut.shouldIncludeInHistory() =
    !excludeFromHistory && !isTemporaryShortcut
