package ch.rmy.android.http_shortcuts.activities.editor

import androidx.compose.runtime.Stable
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

@Stable
data class ShortcutEditorViewState(
    val dialogState: ShortcutEditorDialogState? = null,
    val toolbarSubtitle: Localizable? = null,
    val shortcutExecutionType: ShortcutExecutionType = ShortcutExecutionType.HTTP,
    val shortcutIcon: ShortcutIcon = ShortcutIcon.NoIcon,
    val shortcutName: String = "",
    val shortcutDescription: String = "",
    val isExecutable: Boolean = false,
    val hasChanges: Boolean = false,
    val requestBodyButtonEnabled: Boolean = false,
    val basicSettingsSubtitle: Localizable = Localizable.EMPTY,
    val headersSubtitle: Localizable = Localizable.EMPTY,
    val requestBodySubtitle: Localizable = Localizable.EMPTY,
    val mqttMessagesSubtitle: Localizable = Localizable.EMPTY,
    val authenticationSettingsSubtitle: Localizable = Localizable.EMPTY,
    val scriptingSubtitle: Localizable = Localizable.EMPTY,
    val triggerShortcutsSubtitle: Localizable = Localizable.EMPTY,
    val iconLoading: Boolean = false,
    val isInputDisabled: Boolean = false,
) {
    val isSaveButtonEnabled
        get() = hasChanges && !isInputDisabled

    val isExecuteButtonEnabled
        get() = isExecutable && !isInputDisabled
}
