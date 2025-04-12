package ch.rmy.android.http_shortcuts.activities.main.usecases

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.domains.app_lock.AppLockRepository
import ch.rmy.android.http_shortcuts.utils.BiometricUtil
import javax.inject.Inject

class UnlockAppUseCase
@Inject
constructor(
    private val context: Context,
    private val appLockRepository: AppLockRepository,
    private val biometricUtil: BiometricUtil,
) {
    suspend operator fun invoke(
        showPasswordDialog: () -> Unit,
        onSuccess: () -> Unit,
    ) {
        val lock = appLockRepository.getLock() ?: return
        if (lock.useBiometrics) {
            try {
                val result = biometricUtil.prompt(
                    title = context.getString(R.string.dialog_title_unlock_app),
                    subtitle = context.getString(R.string.subtitle_biometric_app_unlock_prompt),
                    negativeButtonText = context.getString(R.string.button_app_unlock_use_password),
                )
                when (result) {
                    BiometricUtil.Result.SUCCESS -> onSuccess()
                    BiometricUtil.Result.NEGATIVE_BUTTON -> showPasswordDialog()
                }
            } catch (e: BiometricUtil.BiometricException) {
                showPasswordDialog()
            }
        } else {
            showPasswordDialog()
        }
    }
}
