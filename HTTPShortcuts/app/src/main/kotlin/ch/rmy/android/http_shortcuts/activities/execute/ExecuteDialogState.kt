package ch.rmy.android.http_shortcuts.activities.execute

import android.net.Uri
import androidx.compose.runtime.Stable
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogState.RichTextDisplay.ButtonResult
import ch.rmy.android.http_shortcuts.data.enums.ResponseDisplayAction
import java.time.LocalDate
import java.time.LocalTime

@Stable
sealed class ExecuteDialogState<T : Any> {
    @Stable
    data class GenericMessage(
        val message: Localizable,
        val title: Localizable? = null,
    ) : ExecuteDialogState<Unit>()

    @Stable
    data class Warning(
        val message: Localizable,
        val title: Localizable? = null,
        val onHidden: (Boolean) -> Unit,
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

    @Stable
    data class NumberSlider(
        val message: Localizable?,
        val title: Localizable?,
        val initialValue: Float?,
        val min: Float,
        val max: Float,
        val stepSize: Float,
        val prefix: String,
        val suffix: String,
    ) : ExecuteDialogState<Float>()

    @Stable
    data class DatePicker(
        val title: String?,
        val initialDate: LocalDate,
    ) : ExecuteDialogState<LocalDate>()

    @Stable
    data class TimePicker(
        val title: String?,
        val initialTime: LocalTime,
    ) : ExecuteDialogState<LocalTime>()

    @Stable
    data class RichTextDisplay(
        val message: String,
        val title: String?,
        val buttons: List<String>?,
    ) : ExecuteDialogState<ButtonResult>() {
        enum class ButtonResult {
            OK,
            BUTTON1,
            BUTTON2,
        }
    }

    @Stable
    data class ShowResult(
        val title: String,
        val action: ResponseDisplayAction?,
        val content: Content,
        val monospace: Boolean,
        val fontSize: Int?,
    ) : ExecuteDialogState<Unit>() {
        @Stable
        sealed class Content {
            data class Text(val text: String, val allowHtml: Boolean) : Content()
            data class Image(val imageUri: Uri) : Content()
        }
    }
}
