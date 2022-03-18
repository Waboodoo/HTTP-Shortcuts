package ch.rmy.android.http_shortcuts.extensions

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
