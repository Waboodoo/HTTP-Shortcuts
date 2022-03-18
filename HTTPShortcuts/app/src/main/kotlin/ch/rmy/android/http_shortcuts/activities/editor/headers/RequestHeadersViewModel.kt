package ch.rmy.android.http_shortcuts.activities.editor.headers

import android.app.Application
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.swapped
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.models.HeaderModel
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel

class RequestHeadersViewModel(application: Application) : BaseViewModel<Unit, RequestHeadersViewState>(application) {

    private val temporaryShortcutRepository = TemporaryShortcutRepository()
    private val variableRepository = VariableRepository()

    private var headers: List<HeaderModel> = emptyList()
        set(value) {
            field = value
            updateViewState {
                copy(
                    headerItems = mapHeaders(value),
                )
            }
        }

    override fun onInitializationStarted(data: Unit) {
        finalizeInitialization(silent = true)
    }

    override fun initViewState() = RequestHeadersViewState()

    override fun onInitialized() {
        temporaryShortcutRepository.getTemporaryShortcut()
            .subscribe(
                ::initViewStateFromShortcut,
                ::onInitializationError,
            )
            .attachTo(destroyer)

        variableRepository.getObservableVariables()
            .subscribe { variables ->
                updateViewState {
                    copy(variables = variables)
                }
            }
            .attachTo(destroyer)
    }

    private fun initViewStateFromShortcut(shortcut: ShortcutModel) {
        headers = shortcut.headers
    }

    private fun onInitializationError(error: Throwable) {
        handleUnexpectedError(error)
        finish()
    }

    fun onHeaderMoved(headerId1: String, headerId2: String) {
        headers = headers.swapped(headerId1, headerId2) { id }
        performOperation(
            temporaryShortcutRepository.moveHeader(headerId1, headerId2)
        )
    }

    fun onAddHeaderButtonClicked() {
        emitEvent(RequestHeadersEvent.ShowAddHeaderDialog)
    }

    fun onAddHeaderDialogConfirmed(key: String, value: String) {
        temporaryShortcutRepository.addHeader(key, value)
            .compose(progressMonitor.singleTransformer())
            .subscribe { newHeader ->
                headers = headers.plus(newHeader)
            }
            .attachTo(destroyer)
    }

    fun onEditHeaderDialogConfirmed(headerId: String, key: String, value: String) {
        headers = headers
            .map { header ->
                if (header.id == headerId) {
                    HeaderModel(headerId, key, value)
                } else {
                    header
                }
            }
        performOperation(
            temporaryShortcutRepository.updateHeader(headerId, key, value)
        )
    }

    fun onRemoveHeaderButtonClicked(headerId: String) {
        headers = headers.filter { header ->
            header.id != headerId
        }
        performOperation(
            temporaryShortcutRepository.removeHeader(headerId)
        )
    }

    fun onHeaderClicked(id: String) {
        headers.firstOrNull { header ->
            header.id == id
        }
            ?.let { header ->
                emitEvent(RequestHeadersEvent.ShowEditHeaderDialog(id, header.key, header.value))
            }
    }

    fun onBackPressed() {
        waitForOperationsToFinish {
            finish()
        }
    }

    companion object {
        private fun mapHeaders(headers: List<HeaderModel>): List<HeaderListItem> =
            headers.map { header ->
                HeaderListItem.Header(
                    id = header.id,
                    key = header.key,
                    value = header.value,
                )
            }
                .ifEmpty {
                    listOf(HeaderListItem.EmptyState)
                }
    }
}
