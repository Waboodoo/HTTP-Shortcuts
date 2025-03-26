package ch.rmy.android.http_shortcuts.activities.editor.headers

import android.app.Application
import ch.rmy.android.framework.extensions.swapped
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.ViewModelScope
import ch.rmy.android.http_shortcuts.activities.editor.headers.models.HeaderListItem
import ch.rmy.android.http_shortcuts.data.domains.request_headers.RequestHeaderId
import ch.rmy.android.http_shortcuts.data.domains.request_headers.RequestHeaderRepository
import ch.rmy.android.http_shortcuts.data.models.RequestHeader
import ch.rmy.android.http_shortcuts.data.models.Shortcut.Companion.TEMPORARY_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RequestHeadersViewModel
@Inject
constructor(
    application: Application,
    private val requestHeaderRepository: RequestHeaderRepository,
) : BaseViewModel<Unit, RequestHeadersViewState>(application) {

    private var headers: List<RequestHeader> = emptyList()

    private suspend fun updateHeaders(headers: List<RequestHeader>) {
        this.headers = headers
        updateViewState {
            copy(
                headerItems = headers.toHeaderItems(),
            )
        }
    }

    override suspend fun initialize(data: Unit): RequestHeadersViewState {
        headers = requestHeaderRepository.getRequestHeadersByShortcutId(TEMPORARY_ID)
        return RequestHeadersViewState(
            headerItems = headers.toHeaderItems(),
        )
    }

    private fun List<RequestHeader>.toHeaderItems() =
        map { header ->
            HeaderListItem(
                id = header.id,
                key = header.key,
                value = header.value,
            )
        }

    fun onHeaderMoved(headerId1: RequestHeaderId, headerId2: RequestHeaderId) = runAction {
        updateHeaders(headers.swapped(headerId1, headerId2) { id })
        withProgressTracking {
            requestHeaderRepository.moveRequestHeader(headerId1, headerId2)
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
            val newHeader = requestHeaderRepository.insertRequestHeader(key, value)
            updateHeaders(headers.plus(newHeader))
        }
    }

    private suspend fun ViewModelScope<*>.onEditHeaderDialogConfirmed(headerId: RequestHeaderId, key: String, value: String) {
        updateDialogState(null)
        updateHeaders(
            headers
                .map { header ->
                    if (header.id == headerId) {
                        RequestHeader(
                            id = headerId,
                            shortcutId = TEMPORARY_ID,
                            key = key,
                            value = value,
                        )
                    } else {
                        header
                    }
                },
        )
        withProgressTracking {
            requestHeaderRepository.updateRequestHeader(headerId, key, value)
        }
    }

    fun onRemoveHeaderButtonClicked() = runAction {
        val headerId = (viewState.dialogState as? RequestHeadersDialogState.EditHeader)?.id ?: skipAction()
        updateDialogState(null)
        updateHeaders(
            headers.filter { header ->
                header.id != headerId
            },
        )
        withProgressTracking {
            requestHeaderRepository.deleteRequestHeader(headerId)
        }
    }

    fun onHeaderClicked(id: RequestHeaderId) = runAction {
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
                    ),
                )
            }
    }

    fun onBackPressed() = runAction {
        waitForOperationsToFinish()
        closeScreen()
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
