package ch.rmy.android.http_shortcuts.extensions

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.dtos.LauncherShortcut
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel

val ShortcutModel.type: ShortcutExecutionType
    get() = ShortcutExecutionType.get(executionType!!)

fun ShortcutModel.toLauncherShortcut() =
    LauncherShortcut(
        id = id,
        name = name,
        icon = icon,
    )

fun ShortcutModel.getSafeName(context: Context) =
    name.ifEmpty { context.getString(R.string.shortcut_safe_name) }

val ShortcutModel.isTemporaryShortcut
    get() = id == ShortcutModel.TEMPORARY_ID

fun ShortcutModel.shouldIncludeInHistory() =
    !excludeFromHistory && !isTemporaryShortcut
