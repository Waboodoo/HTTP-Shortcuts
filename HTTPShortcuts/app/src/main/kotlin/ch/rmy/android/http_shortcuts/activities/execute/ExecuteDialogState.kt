package ch.rmy.android.http_shortcuts.activities.execute

import androidx.compose.runtime.Stable
import ch.rmy.android.framework.utils.localization.Localizable

@Stable
sealed class ExecuteDialogState<T : Any> {
    @Stable
    data class GenericMessage(
        val message: Localizable,
        val title: Localizable? = null,
    ) : ExecuteDialogState<Unit>()

    @Stable
    data class GenericConfirm(
        val message: Localizable,
        val title: Localizable? = null,
        val confirmButton: Localizable? = null,
    ) : ExecuteDialogState<Unit>()

    @Stable
    data class ColorPicker(
        val title: Localizable? = null,
        val initialColor: Int? = null,
    ) : ExecuteDialogState<Int>()

    @Stable
    data class TextInput(
        val message: Localizable? = null,
        val title: Localizable? = null,
        val initialValue: String? = null,
        val type: Type,
    ) : ExecuteDialogState<String>() {
        enum class Type {
            TEXT,
            MULTILINE_TEXT,
            NUMBER,
            PASSWORD,
        }
    }

    @Stable
    data class Selection(
        val title: Localizable? = null,
        val values: List<Pair<String, String>>,
    ) : ExecuteDialogState<String>()

    @Stable
    data class MultiSelection(
        val title: Localizable? = null,
        val values: List<Pair<String, String>>,
    ) : ExecuteDialogState<List<String>>()
}
