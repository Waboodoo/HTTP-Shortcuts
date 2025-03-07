package ch.rmy.android.http_shortcuts.activities.execute.usecases

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.exceptions.UserException
import ch.rmy.android.http_shortcuts.utils.BiometricUtil
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

class RequestBiometricConfirmationUseCase
@Inject
constructor(
    private val context: Context,
    private val biometricUtil: BiometricUtil,
) {
    suspend operator fun invoke(shortcutName: String) {
        val result = try {
            biometricUtil.prompt(
                title = shortcutName,
                subtitle = context.getString(R.string.subtitle_biometric_confirmation_prompt),
                negativeButtonText = context.getString(R.string.dialog_cancel),
            )
        } catch (e: BiometricUtil.BiometricException) {
            throw UserException.create {
                getString(R.string.error_biometric_confirmation_failed, e.message)
            }
        }

        when (result) {
            BiometricUtil.Result.SUCCESS -> Unit
            BiometricUtil.Result.NEGATIVE_BUTTON -> throw CancellationException()
        }
    }
}
