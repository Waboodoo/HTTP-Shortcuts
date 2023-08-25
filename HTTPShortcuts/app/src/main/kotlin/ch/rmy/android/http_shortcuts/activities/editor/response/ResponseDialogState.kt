package ch.rmy.android.http_shortcuts.activities.editor.response

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.enums.ResponseDisplayAction

@Stable
sealed class ResponseDialogState {
    data class SelectActions(
        val actions: Collection<ResponseDisplayAction>,
    ) : ResponseDialogState()
}
