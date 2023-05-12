package ch.rmy.android.http_shortcuts.activities.editor.body

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.swapped
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.editor.body.models.ParameterListItem
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.enums.ParameterType
import ch.rmy.android.http_shortcuts.data.enums.RequestBodyType
import ch.rmy.android.http_shortcuts.data.models.Parameter
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import com.google.gson.JsonParseException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RequestBodyViewModel(application: Application) : BaseViewModel<Unit, RequestBodyViewState>(application) {

    @Inject
    lateinit var temporaryShortcutRepository: TemporaryShortcutRepository

    init {
        getApplicationComponent().inject(this)
    }

    private var parameters: List<Parameter> = emptyList()

    override fun onInitializationStarted(data: Unit) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val temporaryShortcut = temporaryShortcutRepository.getTemporaryShortcut()
                parameters = temporaryShortcut.parameters
                initialViewState = createInitialViewStateFromShortcut(temporaryShortcut)
                withContext(Dispatchers.Main) {
                    finalizeInitialization()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onInitializationError(e)
                }
            }
        }
    }

    private lateinit var initialViewState: RequestBodyViewState

    override fun initViewState() = initialViewState

    private fun createInitialViewStateFromShortcut(shortcut: Shortcut): RequestBodyViewState =
        RequestBodyViewState(
            requestBodyType = shortcut.bodyType,
            bodyContent = shortcut.bodyContent,
            contentType = shortcut.contentType,
            parameters = mapParameters(shortcut.parameters),
        )

    private fun onInitializationError(error: Throwable) {
        handleUnexpectedError(error)
        finish()
    }

    fun onRequestBodyTypeChanged(type: RequestBodyType) {
        if (type == RequestBodyType.X_WWW_FORM_URLENCODE) {
            val parameters = parameters.filter { it.isStringParameter }
            this.parameters = parameters
        }
        updateViewState {
            copy(
                requestBodyType = type,
                parameters = parameters,
            )
        }
        launchWithProgressTracking {
            temporaryShortcutRepository.setRequestBodyType(type)
        }
    }

    private fun updateParameters(parameters: List<Parameter>) {
        this.parameters = parameters
        updateViewState {
            copy(
                parameters = mapParameters(parameters),
            )
        }
    }

    fun onParameterMoved(parameterId1: String, parameterId2: String) {
        updateParameters(parameters.swapped(parameterId1, parameterId2) { id })
        launchWithProgressTracking {
            temporaryShortcutRepository.moveParameter(parameterId1, parameterId2)
        }
    }

    fun onEditParameterDialogConfirmed(key: String, value: String = "", fileName: String = "") {
        val dialogState = (currentViewState?.dialogState as? RequestBodyDialogState.ParameterEditor ?: return)
        val parameterId = dialogState.id
        updateDialogState(null)

        if (parameterId != null) {
            updateParameters(
                parameters
                    .map { parameter ->
                        if (parameter.id == parameterId) {
                            Parameter(
                                id = parameterId,
                                key = key,
                                value = value,
                                parameterType = parameter.parameterType,
                                fileName = parameter.fileName,
                            )
                        } else {
                            parameter
                        }
                    }
            )
            launchWithProgressTracking {
                temporaryShortcutRepository.updateParameter(parameterId, key, value, fileName)
            }
        } else {
            val type = dialogState.type
            launchWithProgressTracking {
                val newParameter = temporaryShortcutRepository.addParameter(type, key, value, fileName)
                updateParameters(parameters.plus(newParameter))
            }
        }
    }

    fun onRemoveParameterButtonClicked() {
        val parameterId = (currentViewState?.dialogState as? RequestBodyDialogState.ParameterEditor)?.id
            ?: return
        updateDialogState(null)
        updateParameters(
            parameters
                .filter { parameter ->
                    parameter.id != parameterId
                }
        )
        launchWithProgressTracking {
            temporaryShortcutRepository.removeParameter(parameterId)
        }
    }

    fun onAddParameterButtonClicked() {
        doWithViewState { viewState ->
            if (viewState.requestBodyType == RequestBodyType.FORM_DATA) {
                updateDialogState(RequestBodyDialogState.ParameterTypePicker)
            } else {
                onParameterTypeSelected(ParameterType.STRING)
            }
        }
    }

    fun onParameterTypeSelected(type: ParameterType) {
        updateDialogState(
            RequestBodyDialogState.ParameterEditor(
                id = null,
                key = "",
                value = "",
                fileName = "",
                type = type,
            )
        )
    }

    fun onParameterClicked(id: String) {
        parameters.firstOrNull { parameter ->
            parameter.id == id
        }
            ?.let { parameter ->
                updateDialogState(
                    RequestBodyDialogState.ParameterEditor(
                        id = parameter.id,
                        key = parameter.key,
                        value = parameter.value,
                        fileName = parameter.fileName,
                        type = parameter.parameterType,
                    )
                )
            }
    }

    fun onContentTypeChanged(contentType: String) {
        updateViewState {
            copy(contentType = contentType)
        }
        launchWithProgressTracking {
            temporaryShortcutRepository.setContentType(contentType)
        }
    }

    fun onBodyContentChanged(bodyContent: String) {
        doWithViewState { viewState ->
            if (viewState.contentType.isEmpty() && bodyContent.isJsonObjectStart()) {
                onContentTypeChanged("application/json")
            }
            updateViewState {
                copy(
                    bodyContent = bodyContent,
                    bodyContentError = "",
                )
            }
            launchWithProgressTracking {
                temporaryShortcutRepository.setBodyContent(bodyContent)
            }
        }
    }

    fun onBackPressed() {
        viewModelScope.launch {
            waitForOperationsToFinish()
            finish()
        }
    }

    fun onDialogDismissed() {
        updateDialogState(null)
    }

    private fun updateDialogState(dialogState: RequestBodyDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }

    fun onFormatButtonClicked() {
        val bodyContent = currentViewState?.bodyContent ?: return
        viewModelScope.launch {
            try {
                val formatted = withContext(Dispatchers.Default) {
                    GsonUtil.prettyPrintOrThrow(bodyContent)
                }
                updateViewState {
                    copy(bodyContent = formatted)
                }
            } catch (e: JsonParseException) {
                showSnackbar(R.string.error_cannot_format_invalid_json)
                GsonUtil.extractErrorMessage(e)?.let { message ->
                    updateViewState {
                        copy(bodyContentError = message)
                    }
                }
            }
        }
    }

    companion object {
        internal fun mapParameters(parameters: List<Parameter>): List<ParameterListItem> =
            parameters.map { parameter ->
                ParameterListItem(
                    id = parameter.id,
                    key = parameter.key,
                    value = parameter.value,
                    type = parameter.parameterType,
                )
            }

        private val JSON_OBJECT_START = "^\\s*\\{\\s*\".*".toRegex()

        internal fun String.isJsonObjectStart() =
            matches(JSON_OBJECT_START)
    }
}
