package ch.rmy.android.http_shortcuts.extensions

import android.content.Context
import android.graphics.Color
import androidx.compose.runtime.Stable
import ch.rmy.android.framework.extensions.fromHexString
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.dtos.ShortcutPlaceholder
import ch.rmy.android.http_shortcuts.data.dtos.VariablePlaceholder
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.CertificatePin as CertificatePinModel
import ch.rmy.android.http_shortcuts.data.models.Section
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.data.models.Widget
import ch.rmy.android.http_shortcuts.http.CertificatePin as HttpCertificatePin

@JvmName(name = "shortcutIds")
fun List<Shortcut>.ids() = map { it.id }

@JvmName(name = "variableIds")
fun List<Variable>.ids() = map { it.id }

@JvmName(name = "variableIds")
fun Set<Variable>.ids() = map { it.id }.toSet()

@JvmName(name = "categoryIds")
fun List<Category>.ids() = map { it.id }

@JvmName(name = "sectionIds")
fun List<Section>.ids() = map { it.id }

fun Shortcut.toShortcutPlaceholder() =
    ShortcutPlaceholder(
        id = id,
        name = name,
        description = description.takeUnlessEmpty(),
        icon = icon,
    )

fun Shortcut.getSafeName(context: Context) =
    name.ifEmpty { context.getString(R.string.shortcut_safe_name) }

val Shortcut.isTemporaryShortcut
    get() = id == Shortcut.TEMPORARY_ID

fun Shortcut.shouldIncludeInHistory() =
    !excludeFromHistory && !isTemporaryShortcut

fun Variable.toVariablePlaceholder() =
    VariablePlaceholder(
        variableId = id,
        variableKey = key,
        variableType = type,
    )

fun CertificatePinModel.toCertificatePin(): HttpCertificatePin =
    HttpCertificatePin(
        pattern = pattern,
        hash = hash.fromHexString(),
    )

fun Widget.labelColorInt() =
    labelColor?.let(Color::parseColor) ?: Color.WHITE

suspend fun ShortcutRepository.shouldUseForegroundService(shortcutId: ShortcutId): Boolean =
    try {
        getShortcutById(shortcutId)
            .runInForegroundService
    } catch (_: NoSuchElementException) {
        false
    }

@Stable
val ShortcutExecutionType.usesUrl: Boolean
    get() = when (this) {
        ShortcutExecutionType.HTTP,
        ShortcutExecutionType.BROWSER,
        -> true
        ShortcutExecutionType.SCRIPTING,
        ShortcutExecutionType.TRIGGER,
        ShortcutExecutionType.MQTT,
        ShortcutExecutionType.WAKE_ON_LAN,
        -> false
    }

@Stable
val ShortcutExecutionType.usesBasicSettingsScreen: Boolean
    get() = when (this) {
        ShortcutExecutionType.HTTP,
        ShortcutExecutionType.BROWSER,
        ShortcutExecutionType.MQTT,
        ShortcutExecutionType.WAKE_ON_LAN,
        -> true
        ShortcutExecutionType.SCRIPTING,
        ShortcutExecutionType.TRIGGER,
        -> false
    }

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

@Stable
val ShortcutExecutionType.canWaitForConnection: Boolean
    get() = isHttpShortcut

@Stable
val ShortcutExecutionType.canUseFiles: Boolean
    get() = isHttpShortcut

@Stable
val ShortcutExecutionType.usesScriptingEditor: Boolean
    get() = when (this) {
        ShortcutExecutionType.HTTP,
        ShortcutExecutionType.BROWSER,
        ShortcutExecutionType.SCRIPTING,
        ShortcutExecutionType.MQTT,
        ShortcutExecutionType.WAKE_ON_LAN,
        -> true
        ShortcutExecutionType.TRIGGER -> false
    }

@Stable
val ShortcutExecutionType.usesTriggerShortcuts: Boolean
    get() = when (this) {
        ShortcutExecutionType.TRIGGER -> true
        ShortcutExecutionType.BROWSER,
        ShortcutExecutionType.SCRIPTING,
        ShortcutExecutionType.HTTP,
        ShortcutExecutionType.MQTT,
        ShortcutExecutionType.WAKE_ON_LAN,
        -> false
    }

@Stable
val ShortcutExecutionType.usesScriptingTestButton: Boolean
    get() = when (this) {
        ShortcutExecutionType.SCRIPTING -> true
        ShortcutExecutionType.BROWSER,
        ShortcutExecutionType.TRIGGER,
        ShortcutExecutionType.HTTP,
        ShortcutExecutionType.MQTT,
        ShortcutExecutionType.WAKE_ON_LAN,
        -> false
    }
