package ch.rmy.android.http_shortcuts.extensions

import android.content.Context
import ch.rmy.android.framework.extensions.fromHexString
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.dtos.ShortcutPlaceholder
import ch.rmy.android.http_shortcuts.data.dtos.VariablePlaceholder
import ch.rmy.android.http_shortcuts.data.enums.FileUploadType
import ch.rmy.android.http_shortcuts.data.enums.ParameterType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.http.CertificatePin
import ch.rmy.android.http_shortcuts.data.models.CertificatePin as CertificatePinModel

val Shortcut.type: ShortcutExecutionType
    get() = ShortcutExecutionType.get(executionType!!)

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
        variableType = variableType,
    )

fun CertificatePinModel.toCertificatePin(): CertificatePin =
    CertificatePin(
        pattern = pattern,
        hash = hash.fromHexString(),
    )

fun List<Category>.findShortcut(shortcutId: ShortcutId): Shortcut? {
    forEach { category ->
        category.shortcuts.forEach { shortcut ->
            if (shortcut.id == shortcutId) {
                return shortcut
            }
        }
    }
    return null
}

fun Shortcut.hasFileParameter(forImage: Boolean? = null): Boolean =
    parameters.any {
        when (it.parameterType) {
            ParameterType.STRING -> false
            ParameterType.FILE -> {
                it.fileUploadOptions?.type != FileUploadType.CAMERA || forImage != false
            }
        }
    }
