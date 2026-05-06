package ch.rmy.android.http_shortcuts.activities.editor.body

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.swapped
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.editor.body.models.ParameterListItem
import ch.rmy.android.http_shortcuts.data.domains.request_parameters.RequestParameterId
import ch.rmy.android.http_shortcuts.data.domains.request_parameters.RequestParameterRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.working_directories.WorkingDirectoryId
import ch.rmy.android.http_shortcuts.data.domains.working_directories.WorkingDirectoryRepository
import ch.rmy.android.http_shortcuts.data.enums.FileUploadType
import ch.rmy.android.http_shortcuts.data.enums.ParameterType
import ch.rmy.android.http_shortcuts.data.enums.RequestBodyType
import ch.rmy.android.http_shortcuts.data.models.RequestParameter
import ch.rmy.android.http_shortcuts.data.models.Shortcut.Companion.TEMPORARY_ID
import ch.rmy.android.http_shortcuts.data.models.WorkingDirectory
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import ch.rmy.android.http_shortcuts.utils.WorkingDirectoryUtil
import com.google.gson.JsonParseException
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class RequestBodyViewModel
@Inject
constructor(
    application: Application,
    private val temporaryShortcutRepository: TemporaryShortcutRepository,
    private val requestParameterRepository: RequestParameterRepository,
    private val workingDirectoryRepository: WorkingDirectoryRepository,
    private val workingDirectoryUtil: WorkingDirectoryUtil,
) : BaseViewModel<Unit, RequestBodyViewState>(application) {

    private var parameters: List<RequestParameter> = emptyList()
    private var workingDirectoriesById: Map<WorkingDirectoryId, WorkingDirectory> = emptyMap()

    override suspend fun initialize(data: Unit): RequestBodyViewState {
        val shortcut = try {
            temporaryShortcutRepository.getTemporaryShortcut()
        } catch (_: NoSuchElementException) {
            terminateInitialization()
        }
        workingDirectoriesById = workingDirectoryRepository.getWorkingDirectories().associateBy { it.id }
        val directoryName = shortcut.fileUploadSourceDirectoryId
            ?.let { id ->
                workingDirectoriesById[id]?.name
            }
        parameters = requestParameterRepository.getRequestParametersByShortcutId(TEMPORARY_ID)
        viewModelScope.launch {
            workingDirectoryRepository.observeWorkingDirectories()
                .collect { workingDirectories ->
                    workingDirectoriesById = workingDirectories.associateBy { it.id }
                }
        }

        if (shortcut.fileUploadType == FileUploadType.FILE) {
            shortcut.fileUploadSourceDirectoryId?.let { workingDirectoryId ->
                runAction {
                    val suggestions = withContext(Dispatchers.IO) {
                        findFileNameSuggestions(workingDirectoryId)
                    }
                    updateViewState {
                        copy(fileNameSuggestions = suggestions)
                    }
                }
            }
        }

        return RequestBodyViewState(
            requestBodyType = shortcut.requestBodyType,
            fileUploadType = shortcut.fileUploadType ?: FileUploadType.FILE_PICKER,
            bodyContent = shortcut.bodyContent,
            contentType = shortcut.contentType,
            parameters = mapParameters(parameters),
            useImageEditor = shortcut.fileUploadUseImageEditor,
            sourceDirectoryName = directoryName,
            sourceFileName = shortcut.fileUploadSourceFileName
                ?.takeIf { directoryName != null }
                ?: "",
            fileNameSuggestions = emptyList(),
        )
    }

    private fun findFileNameSuggestions(workingDirectoryId: WorkingDirectoryId): List<String> =
        workingDirectoriesById[workingDirectoryId]
            ?.let { workingDirectory ->
                workingDirectoryUtil.getFileNames(workingDirectory)
            }
            ?: emptyList()

    fun onRequestBodyTypeChanged(type: RequestBodyType) = runAction {
        if (type == RequestBodyType.X_WWW_FORM_URLENCODE) {
            parameters = parameters.map { parameter ->
                if (parameter.parameterType != ParameterType.STRING) {
                    parameter.copy(
                        parameterType = ParameterType.STRING,
                        fileUploadType = null,
                        fileUploadFileName = null,
                        fileUploadSourceDirectoryId = null,
                        fileUploadSourceFileName = null,
                        fileUploadUseImageEditor = false,
                    )
                } else {
                    parameter
                }
            }
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

    private suspend fun updateParameters(parameters: List<RequestParameter>) {
        this.parameters = parameters
        updateViewState {
            copy(
                parameters = mapParameters(parameters),
            )
        }
    }

    fun onParameterMoved(parameterId1: RequestParameterId, parameterId2: RequestParameterId) = runAction {
        updateParameters(parameters.swapped(parameterId1, parameterId2) { id })
        withProgressTracking {
            requestParameterRepository.moveRequestParameter(parameterId1, parameterId2)
        }
    }

    fun onEditParameterDialogConfirmed(
        key: String,
        value: String = "",
        fileName: String = "",
        sourceFileName: String = "",
        useImageEditor: Boolean = false,
    ) =
        runAction {
            val dialogState = (viewState.dialogState as? RequestBodyDialogState.ParameterEditor ?: skipAction())
            val parameterId = dialogState.id
            val workingDirectoryId = dialogState.sourceDirectoryId
            updateDialogState(null)
            if (parameterId != null) {
                updateParameters(
                    parameters
                        .map { parameter ->
                            if (parameter.id == parameterId) {
                                parameter.copy(
                                    id = parameterId,
                                    key = key,
                                    value = value,
                                    fileUploadType = dialogState.fileUploadType,
                                    fileUploadFileName = fileName,
                                    fileUploadSourceDirectoryId = workingDirectoryId,
                                    fileUploadSourceFileName = sourceFileName,
                                    fileUploadUseImageEditor = useImageEditor,
                                )
                            } else {
                                parameter
                            }
                        },
                )
                withProgressTracking {
                    requestParameterRepository.updateRequestParameter(
                        parameterId = parameterId,
                        key = key,
                        value = value,
                        fileUploadType = dialogState.fileUploadType,
                        fileUploadFileName = fileName,
                        fileUploadSourceDirectoryId = workingDirectoryId,
                        fileUploadSourceFileName = sourceFileName,
                        fileUploadUseImageEditor = useImageEditor,
                    )
                }
            } else {
                withProgressTracking {
                    val newParameter = requestParameterRepository.insertRequestParameter(
                        key = key,
                        value = value,
                        parameterType = dialogState.type,
                        fileUploadType = dialogState.fileUploadType,
                        fileUploadFileName = fileName,
                        fileUploadSourceDirectoryId = workingDirectoryId,
                        fileUploadSourceFileName = sourceFileName,
                        fileUploadUseImageEditor = useImageEditor,
                    )
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
                },
        )
        withProgressTracking {
            requestParameterRepository.deleteRequestParameter(parameterId)
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
                sourceFileName = "",
                fileNameSuggestions = emptyList(),
            ),
        )
    }

    fun onParameterClicked(id: RequestParameterId) = runAction {
        parameters.firstOrNull { parameter ->
            parameter.id == id
        }
            ?.let { parameter ->
                updateDialogState(
                    RequestBodyDialogState.ParameterEditor(
                        id = parameter.id,
                        key = parameter.key,
                        value = parameter.value,
                        fileName = parameter.fileUploadFileName ?: "",
                        type = parameter.parameterType,
                        useImageEditor = parameter.fileUploadUseImageEditor,
                        fileUploadType = parameter.fileUploadType ?: FileUploadType.FILE_PICKER,
                        sourceDirectoryId = parameter.fileUploadSourceDirectoryId,
                        sourceDirectoryName = parameter.fileUploadSourceDirectoryId?.let {
                            workingDirectoriesById[it]?.name
                        },
                        sourceFileName = parameter.fileUploadSourceFileName ?: "",
                        fileNameSuggestions = parameter.fileUploadSourceDirectoryId?.let { id ->
                            withContext(Dispatchers.IO) {
                                findFileNameSuggestions(id)
                            }
                        } ?: emptyList(),
                    ),
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
        closeScreen()
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

    fun onBodySourceDirectoryNameClicked() = runAction {
        navigate(NavigationDestination.WorkingDirectories.buildRequest(picker = true))
    }

    fun onParameterSourceDirectoryNameClicked() = runAction {
        navigate(NavigationDestination.WorkingDirectories.buildRequest(picker = true))
    }

    fun onBodySourceFileNameChanged(name: String) = runAction {
        updateViewState {
            copy(sourceFileName = name)
        }
        withProgressTracking {
            temporaryShortcutRepository.setFileUploadSourceFileName(name)
        }
    }

    fun onFileUploadTypeChanged(fileUploadType: FileUploadType) = runAction {
        updateViewState {
            copy(
                fileUploadType = fileUploadType,
                sourceDirectoryName = if (fileUploadType == FileUploadType.FILE) {
                    sourceDirectoryName
                } else {
                    null
                },
                sourceFileName = if (fileUploadType == FileUploadType.FILE) {
                    sourceFileName
                } else {
                    ""
                },
            )
        }
        withProgressTracking {
            temporaryShortcutRepository.setFileUploadType(fileUploadType)
        }
    }

    fun onParameterFileUploadTypeChanged(fileUploadType: FileUploadType) = runAction {
        updateViewState {
            copy(
                dialogState = (dialogState as? RequestBodyDialogState.ParameterEditor)?.copy(
                    fileUploadType = fileUploadType,
                ),
            )
        }
    }

    fun onWorkingDirectoryPicked(id: WorkingDirectoryId, name: String) = runAction {
        val suggestions = withContext(Dispatchers.IO) {
            findFileNameSuggestions(id)
        }
        val dialogState = viewState.dialogState
        if (dialogState is RequestBodyDialogState.ParameterEditor) {
            updateDialogState(
                dialogState.copy(
                    sourceDirectoryId = id,
                    sourceDirectoryName = name,
                    fileNameSuggestions = suggestions,
                ),
            )
        } else {
            updateViewState {
                copy(
                    sourceDirectoryName = name,
                    sourceFileName = "",
                    fileNameSuggestions = suggestions,
                )
            }
            withProgressTracking {
                temporaryShortcutRepository.setSourceFileWorkingDirectoryId(id)
            }
        }
    }

    companion object {
        internal fun mapParameters(parameters: List<RequestParameter>): List<ParameterListItem> =
            parameters.map { parameter ->
                ParameterListItem(
                    id = parameter.id,
                    key = parameter.key,
                    value = parameter.value,
                    type = parameter.parameterType,
                    fileUploadType = parameter.fileUploadType,
                )
            }

        private val JSON_OBJECT_START = "^\\s*\\{\\s*\".*".toRegex(RegexOption.DOT_MATCHES_ALL)

        internal fun String.isJsonObjectStart() =
            matches(JSON_OBJECT_START)
    }
}
