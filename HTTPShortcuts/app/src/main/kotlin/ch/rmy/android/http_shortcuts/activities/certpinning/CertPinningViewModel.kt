package ch.rmy.android.http_shortcuts.activities.certpinning

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.certpinning.models.Pin
import ch.rmy.android.http_shortcuts.data.domains.certificate_pins.CertificatePinId
import ch.rmy.android.http_shortcuts.data.domains.certificate_pins.CertificatePinRepository
import ch.rmy.android.http_shortcuts.data.models.CertificatePin
import ch.rmy.android.http_shortcuts.utils.ExternalURLs
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class CertPinningViewModel
@Inject
constructor(
    application: Application,
    private val certificatePinRepository: CertificatePinRepository,
) : BaseViewModel<Unit, CertPinningViewState>(application) {

    private lateinit var pins: List<CertificatePin>
    private var activePinId: CertificatePinId? = null

    override suspend fun initialize(data: Unit): CertPinningViewState {
        val pinsFlow = certificatePinRepository.observeCertificatePins()
        pins = pinsFlow.first()
        viewModelScope.launch {
            pinsFlow.collect { pins ->
                this@CertPinningViewModel.pins = pins
                updateViewState {
                    copy(
                        pins = pins.map {
                            Pin(
                                id = it.id,
                                pattern = it.pattern,
                                hash = it.hash,
                            )
                        },
                    )
                }
            }
        }

        return CertPinningViewState()
    }

    fun onCreatePinButtonClicked() = runAction {
        activePinId = null
        updateDialogState(
            CertPinningDialogState.Editor(
                initialPattern = "",
                initialHash = "",
            ),
        )
    }

    fun onPinClicked(id: CertificatePinId) = runAction {
        activePinId = id
        updateDialogState(CertPinningDialogState.ContextMenu)
    }

    fun onEditOptionSelected() = runAction {
        val id = activePinId ?: skipAction()
        val pin = pins.find { it.id == id } ?: skipAction()
        updateDialogState(
            CertPinningDialogState.Editor(
                initialPattern = pin.pattern,
                initialHash = pin.hash,
            ),
        )
    }

    fun onEditConfirmed(pattern: String, hash: String) = runAction {
        updateDialogState(null)
        val pinId = activePinId
        withProgressTracking {
            if (pinId == null) {
                certificatePinRepository.createCertificatePin(pattern, hash)
            } else {
                certificatePinRepository.updateCertificatePin(pinId, pattern, hash)
            }
        }
    }

    fun onDeleteOptionSelected() = runAction {
        updateDialogState(CertPinningDialogState.ConfirmDeletion)
    }

    fun onDeletionConfirmed() = runAction {
        val id = activePinId ?: skipAction()
        updateDialogState(null)
        withProgressTracking {
            certificatePinRepository.deleteCertificatePinning(id)
            showSnackbar(R.string.message_certificate_pinning_deleted)
        }
    }

    fun onDialogDismissed() = runAction {
        updateDialogState(null)
    }

    private suspend fun updateDialogState(dialogState: CertPinningDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }

    fun onHelpButtonClicked() = runAction {
        openURL(ExternalURLs.CERTIFICATE_PINNING_DOCUMENTATION)
    }
}
