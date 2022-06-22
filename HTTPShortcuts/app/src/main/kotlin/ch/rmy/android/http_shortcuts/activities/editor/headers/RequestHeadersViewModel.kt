package ch.rmy.android.http_shortcuts.activities.editor.headers

import android.app.Application
import android.content.Context
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.swapped
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.models.HeaderModel
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.usecases.GetKeyValueDialogUseCase
import ch.rmy.android.http_shortcuts.usecases.KeepVariablePlaceholderProviderUpdatedUseCase
import javax.inject.Inject

class RequestHeadersViewModel(application: Application) : BaseViewModel<Unit, RequestHeadersViewState>(application), WithDialog {

    @Inject
    lateinit var temporaryShortcutRepository: TemporaryShortcutRepository

    @Inject
    lateinit var keepVariablePlaceholderProviderUpdated: KeepVariablePlaceholderProviderUpdatedUseCase

    @Inject
    lateinit var getKeyValueDialog: GetKeyValueDialogUseCase

    init {
        getApplicationComponent().inject(this)
    }

    private var headers: List<HeaderModel> = emptyList()
        set(value) {
            field = value
            updateViewState {
                copy(
                    headerItems = mapHeaders(value),
                )
            }
        }

    override var dialogState: DialogState?
        get() = currentViewState?.dialogState
        set(value) {
            updateViewState {
                copy(dialogState = value)
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

        keepVariablePlaceholderProviderUpdated(::emitCurrentViewState)
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
        showAddHeaderDialog()
    }

    private fun showAddHeaderDialog() {
        showKeyValueDialog(
            title = StringResLocalizable(R.string.title_custom_header_add),
            keyLabel = StringResLocalizable(R.string.label_custom_header_key),
            valueLabel = StringResLocalizable(R.string.label_custom_header_value),
            suggestions = SUGGESTED_KEYS,
            keyValidator = { validateHeaderName(context, it) },
            valueValidator = { validateHeaderValue(context, it) },
            onConfirm = ::onAddHeaderDialogConfirmed,
        )
    }

    private fun showKeyValueDialog(
        title: Localizable,
        keyLabel: Localizable,
        valueLabel: Localizable,
        key: String? = null,
        value: String? = null,
        suggestions: Array<String>? = null,
        keyValidator: (CharSequence) -> String? = { _ -> null },
        valueValidator: (CharSequence) -> String? = { _ -> null },
        onConfirm: (key: String, value: String) -> Unit,
        onRemove: () -> Unit = {},
    ) {
        dialogState = getKeyValueDialog(
            title = title,
            keyLabel = keyLabel,
            valueLabel = valueLabel,
            key = key,
            value = value,
            suggestions = suggestions,
            keyValidator = keyValidator,
            valueValidator = valueValidator,
            onConfirm = onConfirm,
            onRemove = onRemove,
        )
    }

    private fun onAddHeaderDialogConfirmed(key: String, value: String) {
        temporaryShortcutRepository.addHeader(key, value)
            .compose(progressMonitor.singleTransformer())
            .subscribe { newHeader ->
                headers = headers.plus(newHeader)
            }
            .attachTo(destroyer)
    }

    private fun onEditHeaderDialogConfirmed(headerId: String, key: String, value: String) {
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

    private fun onRemoveHeaderButtonClicked(headerId: String) {
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
                showEditHeaderDialog(id, header.key, header.value)
            }
    }

    private fun showEditHeaderDialog(headerId: String, headerKey: String, headerValue: String) {
        showKeyValueDialog(
            title = StringResLocalizable(R.string.title_custom_header_edit),
            keyLabel = StringResLocalizable(R.string.label_custom_header_key),
            valueLabel = StringResLocalizable(R.string.label_custom_header_value),
            key = headerKey,
            value = headerValue,
            suggestions = SUGGESTED_KEYS,
            keyValidator = { validateHeaderName(context, it) },
            valueValidator = { validateHeaderValue(context, it) },
            onConfirm = { newKey: String, newValue: String ->
                onEditHeaderDialogConfirmed(headerId, newKey, newValue)
            },
            onRemove = {
                onRemoveHeaderButtonClicked(headerId)
            },
        )
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

        val SUGGESTED_KEYS = arrayOf(
            "Accept",
            "Accept-Charset",
            "Accept-Encoding",
            "Accept-Language",
            "Accept-Datetime",
            "Authorization",
            "Cache-Control",
            "Connection",
            "Cookie",
            "Content-Length",
            "Content-MD5",
            "Content-Type",
            "Date",
            "Expect",
            "Forwarded",
            "From",
            "Host",
            "If-Match",
            "If-Modified-Since",
            "If-None-Match",
            "If-Range",
            "If-Unmodified-Since",
            "Max-Forwards",
            "Origin",
            "Pragma",
            "Proxy-Authorization",
            "Range",
            "Referer",
            "TE",
            "User-Agent",
            "Upgrade",
            "Via",
            "Warning",
        )

        private fun validateHeaderName(context: Context, name: CharSequence): String? =
            name
                .firstOrNull { c ->
                    c <= '\u0020' || c >= '\u007f'
                }
                ?.let { invalidChar ->
                    context.getString(R.string.error_invalid_character, invalidChar)
                }

        private fun validateHeaderValue(context: Context, value: CharSequence): String? =
            value
                .firstOrNull { c ->
                    (c <= '\u001f' && c != '\t') || c >= '\u007f'
                }
                ?.let { invalidChar ->
                    context.getString(R.string.error_invalid_character, invalidChar)
                }
    }
}
