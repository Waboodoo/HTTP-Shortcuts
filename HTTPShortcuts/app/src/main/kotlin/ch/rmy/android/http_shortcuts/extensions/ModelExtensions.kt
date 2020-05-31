package ch.rmy.android.http_shortcuts.extensions

import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.models.Shortcut

val Shortcut.type: ShortcutExecutionType
    get() = ShortcutExecutionType.get(executionType!!)
