package ch.rmy.android.http_shortcuts.activities.editor

import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

data class ShortcutEditorViewState(
    val dialogState: DialogState? = null,
    val toolbarTitle: Localizable,
    val toolbarSubtitle: Localizable? = null,
    val shortcutExecutionType: ShortcutExecutionType = ShortcutExecutionType.APP,
    val shortcutIcon: ShortcutIcon = ShortcutIcon.NoIcon,
    val shortcutName: String = "",
    val shortcutDescription: String = "",
    val testButtonVisible: Boolean = false,
    val saveButtonVisible: Boolean = false,
    val requestBodyButtonEnabled: Boolean = false,
    val basicSettingsSubtitle: Localizable = Localizable.EMPTY,
    val headersSubtitle: Localizable = Localizable.EMPTY,
    val requestBodySubtitle: Localizable = Localizable.EMPTY,
    val requestBodySettingsSubtitle: Localizable = Localizable.EMPTY,
    val authenticationSettingsSubtitle: Localizable = Localizable.EMPTY,
    val scriptingSubtitle: Localizable = Localizable.EMPTY,
    val triggerShortcutsSubtitle: Localizable = Localizable.EMPTY,
    val iconLoading: Boolean = false,
) {
    val isIconClickable
        get() = !iconLoading
}
