package ch.rmy.android.http_shortcuts.extensions

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.dtos.GlobalVariablePlaceholder
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.GlobalVariable
import ch.rmy.android.http_shortcuts.data.models.Section
import ch.rmy.android.http_shortcuts.data.models.Shortcut

@JvmName(name = "shortcutIds")
fun List<Shortcut>.ids() = map { it.id }

@JvmName(name = "globalVariableIds")
fun List<GlobalVariable>.ids() = map { it.id }

@JvmName(name = "categoryIds")
fun List<Category>.ids() = map { it.id }

@JvmName(name = "sectionIds")
fun List<Section>.ids() = map { it.id }

fun GlobalVariable.toVariablePlaceholder() =
    GlobalVariablePlaceholder(
        globalVariableId = id,
        variableKey = key,
        variableType = type,
    )

@Stable
val ShortcutExecutionType.isHttpShortcut: Boolean
    get() = when (this) {
        ShortcutExecutionType.HTTP -> true
        ShortcutExecutionType.BROWSER,
        ShortcutExecutionType.SCRIPTING,
        ShortcutExecutionType.TRIGGER,
        ShortcutExecutionType.MQTT,
        ShortcutExecutionType.WAKE_ON_LAN,
        -> false
    }

@Stable
val ShortcutExecutionType.usesResponse: Boolean
    get() = isHttpShortcut
