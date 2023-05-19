package ch.rmy.android.http_shortcuts.activities.execute

import androidx.compose.runtime.Stable
import ch.rmy.android.framework.utils.localization.Localizable

@Stable
sealed class ExecuteDialogState {
    @Stable
    data class GenericMessage(
        val message: Localizable,
        val title: Localizable? = null,
    ) : ExecuteDialogState()

    @Stable
    data class GenericConfirm(
        val message: Localizable,
        val title: Localizable? = null,
        val confirmButton: Localizable? = null,
    ) : ExecuteDialogState()

    @Stable
    data class ColorPicker(
        val title: Localizable? = null,
        val initialColor: Int? = null,
    ) : ExecuteDialogState()
}
