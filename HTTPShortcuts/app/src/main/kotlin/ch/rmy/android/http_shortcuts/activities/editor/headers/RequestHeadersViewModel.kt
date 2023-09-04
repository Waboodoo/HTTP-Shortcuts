package ch.rmy.android.http_shortcuts.activities.editor.headers

import android.app.Application
import ch.rmy.android.framework.extensions.swapped
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.ViewModelScope
import ch.rmy.android.http_shortcuts.activities.editor.headers.models.HeaderListItem
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.models.Header
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RequestHeadersViewModel
@Inject
constructor(
    application: Application,
    private val temporaryShortcutRepository: TemporaryShortcutRepository,
) : BaseViewModel<Unit, RequestHeadersViewState>(application) {

    private var headers: List<Header> = emptyList()

    private suspend fun updateHeaders(headers: List<Header>) {
        this.headers = headers
        updateViewState {
            copy(
                headerItems = headers.toHeaderItems(),
            )
        }
    }

    override suspend fun initialize(data: Unit): RequestHeadersViewState {
        val shortcut = temporaryShortcutRepository.getTemporaryShortcut()
        headers = shortcut.headers
        return RequestHeadersViewState(
            headerItems = shortcut.headers.toHeaderItems(),
        )
    }

    private fun List<Header>.toHeaderItems() =
        map { header ->
            HeaderListItem(
                id = header.id,
                key = header.key,
                value = header.value,
            )
        }

    fun onHeaderMoved(headerId1: String, headerId2: String) = runAction {
        updateHeaders(headers.swapped(headerId1, headerId2) { id })
        withProgressTracking {
            temporaryShortcutRepository.moveHeader(headerId1, headerId2)
        }
    }

    fun onAddHeaderButtonClicked() = runAction {
        updateDialogState(RequestHeadersDialogState.AddHeader)
    }

    fun onDialogConfirmed(key: String, value: String) = runAction {
        when (val dialogState = viewState.dialogState) {
            is RequestHeadersDialogState.AddHeader -> onAddHeaderDialogConfirmed(key, value)
            is RequestHeadersDialogState.EditHeader -> onEditHeaderDialogConfirmed(dialogState.id, key, value)
            else -> Unit
        }
    }

    private suspend fun ViewModelScope<*>.onAddHeaderDialogConfirmed(key: String, value: String) {
        updateDialogState(null)
        withProgressTracking {
            val newHeader = temporaryShortcutRepository.addHeader(key, value)
            updateHeaders(headers.plus(newHeader))
        }
    }

    private suspend fun ViewModelScope<*>.onEditHeaderDialogConfirmed(headerId: String, key: String, value: String) {
        updateDialogState(null)
        updateHeaders(
            headers
                .map { header ->
                    if (header.id == headerId) {
                        Header(headerId, key, value)
                    } else {
                        header
                    }
                }
        )
        withProgressTracking {
            temporaryShortcutRepository.updateHeader(headerId, key, value)
        }
    }

    fun onRemoveHeaderButtonClicked() = runAction {
        val headerId = (viewState.dialogState as? RequestHeadersDialogState.EditHeader)?.id ?: skipAction()
        updateDialogState(null)
        updateHeaders(
            headers.filter { header ->
                header.id != headerId
            }
        )
        withProgressTracking {
            temporaryShortcutRepository.removeHeader(headerId)
        }
    }

    fun onHeaderClicked(id: String) = runAction {
        headers
            .firstOrNull { header ->
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

    fun onBackPressed() = runAction {
        waitForOperationsToFinish()
        finish()
    }

    fun onDismissDialog() = runAction {
        updateDialogState(null)
    }

    private suspend fun updateDialogState(dialogState: RequestHeadersDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }
}
