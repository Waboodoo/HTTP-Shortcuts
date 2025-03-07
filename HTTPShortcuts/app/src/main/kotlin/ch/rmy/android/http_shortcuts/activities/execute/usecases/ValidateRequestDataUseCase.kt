package ch.rmy.android.http_shortcuts.activities.execute.usecases

import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogState
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.exceptions.DialogCancellationException
import ch.rmy.android.http_shortcuts.http.RequestData
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import ch.rmy.android.http_shortcuts.utils.Settings
import com.google.gson.JsonParseException
import javax.inject.Inject

class ValidateRequestDataUseCase
@Inject
constructor(
    private val settings: Settings,
) {
    suspend operator fun invoke(dialogHandle: DialogHandle, shortcut: Shortcut, requestData: RequestData) {
        if (!shortcut.usesCustomBody()) {
            return
        }
        if (requestData.contentType?.startsWith("application/json", ignoreCase = true) != true) {
            return
        }
        if (settings.isMalformedJsonWarningPermanentlyHidden) {
            return
        }
        try {
            GsonUtil.prettyPrintOrThrow(requestData.body)
        } catch (e: JsonParseException) {
            GsonUtil.extractErrorMessage(e)
                ?.let { errorMessage ->
                    try {
                        dialogHandle.showDialog(
                            ExecuteDialogState.Warning(
                                title = StringResLocalizable(R.string.warning_dialog_title),
                                message = StringResLocalizable(R.string.warning_message_malformed_json, errorMessage),
                                onHidden = {
                                    settings.isMalformedJsonWarningPermanentlyHidden = it
                                },
                            ),
                        )
                    } catch (e: DialogCancellationException) {
                        // Continue as normal
                    }
                }
        }
    }
}
