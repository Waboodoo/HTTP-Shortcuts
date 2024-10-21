package ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets

import androidx.compose.runtime.Stable
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.http_shortcuts.data.dtos.ShortcutPlaceholder
import ch.rmy.android.http_shortcuts.data.dtos.VariablePlaceholder

@Stable
sealed class CodeSnippetPickerDialogState {
    @Stable
    data object SelectIcon : CodeSnippetPickerDialogState()

    @Stable
    data class SelectShortcut(
        val title: Localizable,
        val shortcuts: List<ShortcutPlaceholder>,
    ) : CodeSnippetPickerDialogState()

    @Stable
    data class SelectVariableForReading(
        val variables: List<VariablePlaceholder>,
    ) : CodeSnippetPickerDialogState()

    @Stable
    data class SelectVariableForWriting(
        val variables: List<VariablePlaceholder>,
    ) : CodeSnippetPickerDialogState()

    @Stable
    data class SelectWorkingDirectory(
        val directoryNames: List<String>,
    ) : CodeSnippetPickerDialogState()
}
