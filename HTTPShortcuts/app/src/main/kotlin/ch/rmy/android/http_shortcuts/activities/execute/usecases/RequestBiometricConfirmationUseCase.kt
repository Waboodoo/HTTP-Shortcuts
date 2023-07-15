package ch.rmy.android.http_shortcuts.activities.execute.usecases

import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import ch.rmy.android.framework.extensions.resume
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.exceptions.UserException
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resumeWithException

class RequestBiometricConfirmationUseCase
@Inject
constructor(
    private val activityProvider: ActivityProvider,
) {
    suspend operator fun invoke(shortcutName: String) {
        suspendCancellableCoroutine<Unit> { continuation ->
            val activity = activityProvider.getActivity()
            val executor = ContextCompat.getMainExecutor(activity)
            val biometricPrompt = BiometricPrompt(
                activity, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        when (errorCode) {
                            BiometricPrompt.ERROR_USER_CANCELED,
                            BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                            -> {
                                continuation.cancel()
                            }
                            else -> {
                                continuation.resumeWithException(
                                    UserException.create {
                                        getString(R.string.error_biometric_confirmation_failed, errString)
                                    }
                                )
                            }
                        }
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        continuation.resume()
                    }
                }
            )

            biometricPrompt.authenticate(
                BiometricPrompt.PromptInfo.Builder()
                    .setTitle(shortcutName)
                    .setSubtitle(activity.getString(R.string.subtitle_biometric_confirmation_prompt))
                    .setConfirmationRequired(false)
                    .setNegativeButtonText(activity.getString(R.string.dialog_cancel))
                    .build()
            )
        }
    }
}
