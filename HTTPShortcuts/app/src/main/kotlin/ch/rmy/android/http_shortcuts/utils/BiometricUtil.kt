package ch.rmy.android.http_shortcuts.utils

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import ch.rmy.android.http_shortcuts.exceptions.NoActivityAvailableException
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

class BiometricUtil
@Inject
constructor(
    private val activityProvider: ActivityProvider,
) {
    suspend fun prompt(
        title: String,
        subtitle: String,
        negativeButtonText: String,
    ): Result =
        withContext(Dispatchers.Main) {
            activityProvider.withActivity { activity ->
                suspendCancellableCoroutine { continuation ->
                    val executor = ContextCompat.getMainExecutor(activity)
                    val biometricPrompt = BiometricPrompt(
                        activity,
                        executor,
                        object : BiometricPrompt.AuthenticationCallback() {
                            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                super.onAuthenticationError(errorCode, errString)
                                when (errorCode) {
                                    BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                                        continuation.resume(Result.NEGATIVE_BUTTON)
                                    }
                                    BiometricPrompt.ERROR_USER_CANCELED -> {
                                        continuation.cancel()
                                    }
                                    else -> {
                                        continuation.resumeWithException(BiometricException(errString.toString()))
                                    }
                                }
                            }

                            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                super.onAuthenticationSucceeded(result)
                                continuation.resume(Result.SUCCESS)
                            }
                        },
                    )

                    biometricPrompt.authenticate(
                        BiometricPrompt.PromptInfo.Builder()
                            .setTitle(title)
                            .setSubtitle(subtitle)
                            .setConfirmationRequired(false)
                            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                            .setNegativeButtonText(negativeButtonText)
                            .build(),
                    )
                }
            }
        }

    suspend fun canUseBiometrics(): Boolean =
        try {
            activityProvider.withActivity { activity ->
                BiometricManager.from(activity)
                    .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
            }
        } catch (e: NoActivityAvailableException) {
            false
        }

    class BiometricException(override val message: String) : Exception()

    enum class Result {
        SUCCESS,
        NEGATIVE_BUTTON,
    }
}
