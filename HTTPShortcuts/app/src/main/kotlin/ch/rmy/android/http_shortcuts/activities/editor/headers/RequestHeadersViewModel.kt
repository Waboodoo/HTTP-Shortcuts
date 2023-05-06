package ch.rmy.android.http_shortcuts.activities.editor.headers

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.swapped
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.activities.editor.headers.models.HeaderListItem
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.models.Header
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import javax.inject.Inject

class RequestHeadersViewModel(application: Application) : BaseViewModel<Unit, RequestHeadersViewState>(application) {

    @Inject
    lateinit var temporaryShortcutRepository: TemporaryShortcutRepository

    init {
        getApplicationComponent().inject(this)
    }

    private var headers: List<Header> = emptyList()
        set(value) {
            field = value
            updateViewState {
                copy(
                    headerItems = value.map { header ->
                        HeaderListItem(
                            id = header.id,
                            key = header.key,
                            value = header.value,
                        )
                    },
                )
            }
        }

    override fun onInitializationStarted(data: Unit) {
        finalizeInitialization(silent = true)
    }

    override fun initViewState() = RequestHeadersViewState()

    override fun onInitialized() {
        viewModelScope.launch {
            try {
                val temporaryShortcut = temporaryShortcutRepository.getTemporaryShortcut()
                initViewStateFromShortcut(temporaryShortcut)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                onInitializationError(e)
            }
        }
    }

    private fun initViewStateFromShortcut(shortcut: Shortcut) {
        headers = shortcut.headers
    }

    private fun onInitializationError(error: Throwable) {
        handleUnexpectedError(error)
        finish()
    }

    fun onHeaderMoved(headerId1: String, headerId2: String) {
        headers = headers.swapped(headerId1, headerId2) { id }
        launchWithProgressTracking {
            temporaryShortcutRepository.moveHeader(headerId1, headerId2)
        }
    }

    fun onAddHeaderButtonClicked() {
        updateDialogState(RequestHeadersDialogState.AddHeader)
    }

    fun onDialogConfirmed(key: String, value: String) {
        when (val dialogState = currentViewState?.dialogState) {
            is RequestHeadersDialogState.AddHeader -> onAddHeaderDialogConfirmed(key, value)
            is RequestHeadersDialogState.EditHeader -> onEditHeaderDialogConfirmed(dialogState.id, key, value)
            else -> Unit
        }
    }

    private fun onAddHeaderDialogConfirmed(key: String, value: String) {
        updateDialogState(null)
        launchWithProgressTracking {
            val newHeader = temporaryShortcutRepository.addHeader(key, value)
            headers = headers.plus(newHeader)
        }
    }

    private fun onEditHeaderDialogConfirmed(headerId: String, key: String, value: String) {
        updateDialogState(null)
        headers = headers
            .map { header ->
                if (header.id == headerId) {
                    Header(headerId, key, value)
                } else {
                    header
                }
            }
        launchWithProgressTracking {
            temporaryShortcutRepository.updateHeader(headerId, key, value)
        }
    }

    fun onRemoveHeaderButtonClicked() {
        val headerId = (currentViewState?.dialogState as? RequestHeadersDialogState.EditHeader)?.id ?: return
        updateDialogState(null)
        headers = headers.filter { header ->
            header.id != headerId
        }
        launchWithProgressTracking {
            temporaryShortcutRepository.removeHeader(headerId)
        }
    }

    fun onHeaderClicked(id: String) {
        headers.firstOrNull { header ->
            header.id == id
        }
            ?.let { header ->
                updateDialogState(
                    RequestHeadersDialogState.EditHeader(
                        id = header.id,
                        key = header.key,
                        value = header.value,
                    )
                )
            }
    }

    fun onBackPressed() {
        viewModelScope.launch {
            waitForOperationsToFinish()
            finish()
        }
    }

    fun onDismissDialog() {
        updateDialogState(null)
    }

    private fun updateDialogState(dialogState: RequestHeadersDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }
}
