package ch.rmy.android.http_shortcuts.activities.certpinning

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.certpinning.models.Pin
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.app.AppRepository
import ch.rmy.android.http_shortcuts.data.models.CertificatePin
import ch.rmy.android.http_shortcuts.utils.ExternalURLs
import kotlinx.coroutines.launch
import javax.inject.Inject

class CertPinningViewModel(application: Application) : BaseViewModel<Unit, CertPinningViewState>(application) {

    @Inject
    lateinit var appRepository: AppRepository

    init {
        getApplicationComponent().inject(this)
    }

    override fun onInitializationStarted(data: Unit) {
        finalizeInitialization(silent = true)
    }

    private lateinit var pins: List<CertificatePin>
    private var activePinId: String? = null

    override fun onInitialized() {
        viewModelScope.launch {
            appRepository.getObservableCertificatePins().collect { pins ->
                this@CertPinningViewModel.pins = pins
                updateViewState {
                    copy(
                        pins = pins.map {
                            Pin(
                                id = it.id,
                                pattern = it.pattern,
                                hash = it.hash,
                            )
                        }
                    )
                }
            }
        }
    }

    override fun initViewState() = CertPinningViewState()

    fun onCreatePinButtonClicked() {
        activePinId = null
        updateDialogState(
            CertPinningDialogState.Editor(
                initialPattern = "",
                initialHash = "",
            )
        )
    }

    fun onPinClicked(id: String) {
        activePinId = id
        updateDialogState(CertPinningDialogState.ContextMenu)
    }

    fun onEditOptionSelected() {
        val id = activePinId ?: return
        val pin = pins.find { it.id == id } ?: return
        updateDialogState(
            CertPinningDialogState.Editor(
                initialPattern = pin.pattern,
                initialHash = pin.hash,
            )
        )
    }

    fun onEditConfirmed(pattern: String, hash: String) {
        updateDialogState(null)
        val pinId = activePinId
        launchWithProgressTracking {
            if (pinId == null) {
                appRepository.createCertificatePin(pattern, hash)
            } else {
                appRepository.updateCertificatePin(pinId, pattern, hash)
            }
        }
    }

    fun onDeleteOptionSelected() {
        updateDialogState(CertPinningDialogState.ConfirmDeletion)
    }

    fun onDeletionConfirmed() {
        val id = activePinId ?: return
        updateDialogState(null)
        launchWithProgressTracking {
            appRepository.deleteCertificatePinning(id)
            showSnackbar(R.string.message_certificate_pinning_deleted)
        }
    }

    fun onDialogDismissed() {
        updateDialogState(null)
    }

    private fun updateDialogState(dialogState: CertPinningDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }

    fun onHelpButtonClicked() {
        openURL(ExternalURLs.CERTIFICATE_PINNING_DOCUMENTATION)
    }
}
