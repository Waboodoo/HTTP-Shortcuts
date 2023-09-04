package ch.rmy.android.http_shortcuts.activities.editor.body

import android.app.Application
import android.net.Uri
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.swapped
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.editor.body.models.ParameterListItem
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.enums.FileUploadType
import ch.rmy.android.http_shortcuts.data.enums.ParameterType
import ch.rmy.android.http_shortcuts.data.enums.RequestBodyType
import ch.rmy.android.http_shortcuts.data.models.FileUploadOptions
import ch.rmy.android.http_shortcuts.data.models.Parameter
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import com.google.gson.JsonParseException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class RequestBodyViewModel
@Inject
constructor(
    application: Application,
    private val temporaryShortcutRepository: TemporaryShortcutRepository,
) : BaseViewModel<Unit, RequestBodyViewState>(application) {

    private var parameters: List<Parameter> = emptyList()

    override suspend fun initialize(data: Unit): RequestBodyViewState {
        val shortcut = temporaryShortcutRepository.getTemporaryShortcut()
        parameters = shortcut.parameters
        return RequestBodyViewState(
            requestBodyType = shortcut.bodyType,
            fileUploadType = shortcut.fileUploadOptions?.type ?: FileUploadType.FILE_PICKER,
            bodyContent = shortcut.bodyContent,
            contentType = shortcut.contentType,
            parameters = mapParameters(shortcut.parameters),
            useImageEditor = shortcut.fileUploadOptions?.useImageEditor == true,
            fileName = shortcut.fileUploadOptions?.file?.toUri()?.getFileName(),
        )
    }

    fun onRequestBodyTypeChanged(type: RequestBodyType) = runAction {
        if (type == RequestBodyType.X_WWW_FORM_URLENCODE) {
            val parameters = parameters.filter { it.isStringParameter }
            this@RequestBodyViewModel.parameters = parameters
        }
        updateViewState {
            copy(
                requestBodyType = type,
                parameters = parameters,
            )
        }
        withProgressTracking {
            temporaryShortcutRepository.setRequestBodyType(type)
        }
    }

    private suspend fun updateParameters(parameters: List<Parameter>) {
        this.parameters = parameters
        updateViewState {
            copy(
                parameters = mapParameters(parameters),
            )
        }
    }

    fun onParameterMoved(parameterId1: String, parameterId2: String) = runAction {
        updateParameters(parameters.swapped(parameterId1, parameterId2) { id })
        withProgressTracking {
            temporaryShortcutRepository.moveParameter(parameterId1, parameterId2)
        }
    }

    fun onEditParameterDialogConfirmed(key: String, value: String = "", fileName: String = "", useImageEditor: Boolean = false) =
        runAction {
            val dialogState = (viewState.dialogState as? RequestBodyDialogState.ParameterEditor ?: skipAction())
            val parameterId = dialogState.id
            updateDialogState(null)
            val fileUploadOptions = FileUploadOptions()
                .apply {
                    this.useImageEditor = useImageEditor
                    this.type = dialogState.fileUploadType
                    this.file = dialogState.sourceFile?.toString()
                }

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
                                    fileUploadOptions = fileUploadOptions,
                                )
                            } else {
                                parameter
                            }
                        }
                )
                withProgressTracking {
                    temporaryShortcutRepository.updateParameter(parameterId, key, value, fileName, fileUploadOptions)
                }
            } else {
                val type = dialogState.type
                withProgressTracking {
                    val newParameter = temporaryShortcutRepository.addParameter(type, key, value, fileName, fileUploadOptions)
                    updateParameters(parameters.plus(newParameter))
                }
            }
        }

    fun onRemoveParameterButtonClicked() = runAction {
        val parameterId = (viewState.dialogState as? RequestBodyDialogState.ParameterEditor)?.id
            ?: skipAction()
        updateDialogState(null)
        updateParameters(
            parameters
                .filter { parameter ->
                    parameter.id != parameterId
                }
        )
        withProgressTracking {
            temporaryShortcutRepository.removeParameter(parameterId)
        }
    }

    fun onAddParameterButtonClicked() = runAction {
        if (viewState.requestBodyType == RequestBodyType.FORM_DATA) {
            updateDialogState(RequestBodyDialogState.ParameterTypePicker)
        } else {
            onParameterTypeSelected(ParameterType.STRING)
        }
    }

    fun onParameterTypeSelected(type: ParameterType) = runAction {
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

    fun onParameterClicked(id: String) = runAction {
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
                        useImageEditor = parameter.fileUploadOptions?.useImageEditor == true,
                        fileUploadType = parameter.fileUploadOptions?.type ?: FileUploadType.FILE_PICKER,
                        sourceFile = parameter.fileUploadOptions?.file?.toUri(),
                        sourceFileName = parameter.fileUploadOptions?.file?.toUri()?.getFileName(),
                    )
                )
            }
    }

    fun onContentTypeChanged(contentType: String) = runAction {
        updateViewState {
            copy(contentType = contentType)
        }
        withProgressTracking {
            temporaryShortcutRepository.setContentType(contentType)
        }
    }

    fun onBodyContentChanged(bodyContent: String) = runAction {
        if (viewState.contentType.isEmpty() && bodyContent.isJsonObjectStart()) {
            onContentTypeChanged("application/json")
        }
        updateViewState {
            copy(
                bodyContent = bodyContent,
                bodyContentError = "",
            )
        }
        withProgressTracking {
            temporaryShortcutRepository.setBodyContent(bodyContent)
        }
    }

    fun onBackPressed() = runAction {
        waitForOperationsToFinish()
        finish()
    }

    fun onDialogDismissed() = runAction {
        updateDialogState(null)
    }

    private suspend fun updateDialogState(dialogState: RequestBodyDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }

    fun onFormatButtonClicked() = runAction {
        val bodyContent = viewState.bodyContent
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

    fun onUseImageEditorChanged(useImageEditor: Boolean) = runAction {
        updateViewState {
            copy(useImageEditor = useImageEditor)
        }
        withProgressTracking {
            temporaryShortcutRepository.setUseImageEditor(useImageEditor)
        }
    }

    fun onFilePickedForBody(fileUri: Uri) = runAction {
        updateViewState {
            copy(
                fileName = fileUri.getFileName(),
                fileUploadType = FileUploadType.FILE,
            )
        }
        withProgressTracking {
            temporaryShortcutRepository.setFileUploadUri(fileUri)
        }
    }

    fun onFilePickedForParameter(fileUri: Uri) = runAction {
        updateViewState {
            copy(
                dialogState = (dialogState as? RequestBodyDialogState.ParameterEditor)?.copy(
                    sourceFile = fileUri,
                    sourceFileName = fileUri.getFileName(),
                    fileUploadType = FileUploadType.FILE,
                )
            )
        }
    }

    fun onFileUploadTypeChanged(fileUploadType: FileUploadType) = runAction {
        if (fileUploadType == FileUploadType.FILE) {
            emitEvent(RequestBodyEvent.PickFileForBody)
            skipAction()
        }
        updateViewState {
            copy(fileUploadType = fileUploadType)
        }
        withProgressTracking {
            temporaryShortcutRepository.setFileUploadType(fileUploadType)
        }
    }

    private fun Uri.getFileName(): String? =
        DocumentFile.fromSingleUri(context, this)?.name

    fun onBodyFileNameClicked() = runAction {
        emitEvent(RequestBodyEvent.PickFileForBody)
    }

    fun onParameterFileUploadTypeChanged(fileUploadType: FileUploadType) = runAction {
        if (fileUploadType == FileUploadType.FILE) {
            emitEvent(RequestBodyEvent.PickFileForParameter)
            skipAction()
        }
        updateViewState {
            copy(
                dialogState = (dialogState as? RequestBodyDialogState.ParameterEditor)?.copy(
                    fileUploadType = fileUploadType,
                )
            )
        }
    }

    fun onParameterFileNameClicked() = runAction {
        emitEvent(RequestBodyEvent.PickFileForParameter)
    }

    companion object {
        internal fun mapParameters(parameters: List<Parameter>): List<ParameterListItem> =
            parameters.map { parameter ->
                ParameterListItem(
                    id = parameter.id,
                    key = parameter.key,
                    value = parameter.value,
                    type = parameter.parameterType,
                    fileUploadType = parameter.fileUploadOptions?.type,
                )
            }

        private val JSON_OBJECT_START = "^\\s*\\{\\s*\".*".toRegex()

        internal fun String.isJsonObjectStart() =
            matches(JSON_OBJECT_START)
    }
}
