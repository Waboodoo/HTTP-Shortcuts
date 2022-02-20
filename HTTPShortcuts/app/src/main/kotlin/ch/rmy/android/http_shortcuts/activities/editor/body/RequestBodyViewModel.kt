package ch.rmy.android.http_shortcuts.activities.editor.body

import android.app.Application
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.swapped
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.enums.RequestBodyType
import ch.rmy.android.http_shortcuts.data.models.Parameter
import ch.rmy.android.http_shortcuts.data.models.Shortcut

class RequestBodyViewModel(application: Application) : BaseViewModel<Unit, RequestBodyViewState>(application) {

    private val temporaryShortcutRepository = TemporaryShortcutRepository()
    private val variableRepository = VariableRepository()

    private var parameters: List<Parameter> = emptyList()
        set(value) {
            field = value
            updateViewState {
                copy(
                    parameters = mapParameters(value),
                )
            }
        }

    override fun onInitializationStarted(data: Unit) {
        finalizeInitialization(silent = true)
    }

    override fun initViewState() = RequestBodyViewState()

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

    private fun initViewStateFromShortcut(shortcut: Shortcut) {
        atomicallyUpdateViewState {
            this.parameters = shortcut.parameters
            updateViewState {
                copy(
                    requestBodyType = shortcut.bodyType,
                    bodyContent = shortcut.bodyContent,
                    contentType = shortcut.contentType,
                )
            }
        }
    }

    private fun onInitializationError(error: Throwable) {
        handleUnexpectedError(error)
        finish()
    }

    fun onRequestBodyTypeChanged(type: RequestBodyType) {
        atomicallyUpdateViewState {
            if (type == RequestBodyType.X_WWW_FORM_URLENCODE) {
                parameters = parameters.filter { it.isStringParameter }
            }
            updateViewState {
                copy(requestBodyType = type)
            }
        }
        performOperation(
            temporaryShortcutRepository.setRequestBodyType(type)
        )
    }

    fun onParameterMoved(parameterId1: String, parameterId2: String) {
        parameters = parameters.swapped(parameterId1, parameterId2) { id }
        performOperation(
            temporaryShortcutRepository.moveParameter(parameterId1, parameterId2)
        )
    }

    fun onAddStringParameterDialogConfirmed(key: String, value: String) {
        temporaryShortcutRepository.addStringParameter(key, value)
            .compose(progressMonitor.singleTransformer())
            .subscribe { newParameter ->
                parameters = parameters.plus(newParameter)
            }
            .attachTo(destroyer)
    }

    fun onAddFileParameterDialogConfirmed(key: String, fileName: String, multiple: Boolean) {
        temporaryShortcutRepository.addFileParameter(key, fileName, multiple)
            .compose(progressMonitor.singleTransformer())
            .subscribe { newParameter ->
                parameters = parameters.plus(newParameter)
            }
            .attachTo(destroyer)
    }

    fun onEditParameterDialogConfirmed(parameterId: String, key: String, value: String = "", fileName: String = "") {
        parameters = parameters
            .map { parameter ->
                if (parameter.id == parameterId) {
                    Parameter(
                        id = parameterId,
                        key = key,
                        value = value,
                        type = parameter.type,
                        fileName = parameter.fileName,
                    )
                } else {
                    parameter
                }
            }
        performOperation(
            temporaryShortcutRepository.updateParameter(parameterId, key, value, fileName)
        )
    }

    fun onRemoveParameterButtonClicked(parameterId: String) {
        parameters = parameters
            .filter { parameter ->
                parameter.id != parameterId
            }
        performOperation(
            temporaryShortcutRepository.removeParameter(parameterId)
        )
    }

    fun onAddParameterButtonClicked() {
        if (currentViewState.requestBodyType == RequestBodyType.FORM_DATA) {
            emitEvent(RequestBodyEvent.ShowAddParameterTypeSelectionDialog)
        } else {
            emitEvent(RequestBodyEvent.ShowAddParameterForStringDialog)
        }
    }

    fun onParameterClicked(id: String) {
        parameters.firstOrNull { parameter ->
            parameter.id == id
        }
            ?.let { parameter ->
                emitEvent(
                    if (parameter.isFileParameter || parameter.isFilesParameter) {
                        RequestBodyEvent.ShowEditParameterForFileDialog(
                            id,
                            parameter.key,
                            showFileNameOption = parameter.isFileParameter,
                            fileName = parameter.fileName,
                        )
                    } else {
                        RequestBodyEvent.ShowEditParameterForStringDialog(
                            id,
                            parameter.key,
                            parameter.value,
                        )
                    }
                )
            }
    }

    fun onContentTypeChanged(contentType: String) {
        updateViewState {
            copy(contentType = contentType)
        }
        performOperation(
            temporaryShortcutRepository.setContentType(contentType)
        )
    }

    fun onBodyContentChanged(bodyContent: String) {
        updateViewState {
            copy(bodyContent = bodyContent)
        }
        performOperation(
            temporaryShortcutRepository.setBodyContent(bodyContent)
        )
    }

    fun onBackPressed() {
        waitForOperationsToFinish {
            finish()
        }
    }

    companion object {
        private fun mapParameters(parameters: List<Parameter>): List<ParameterListItem> =
            parameters.map { parameter ->
                ParameterListItem.Parameter(
                    id = parameter.id,
                    key = parameter.key,
                    value = parameter.value.takeIf { parameter.isStringParameter },
                    label = when {
                        parameter.isFileParameter -> {
                            StringResLocalizable(R.string.subtitle_parameter_value_file)
                        }
                        parameter.isFilesParameter -> {
                            StringResLocalizable(R.string.subtitle_parameter_value_files)
                        }
                        else -> null
                    },
                )
            }
                .ifEmpty {
                    listOf(ParameterListItem.EmptyState)
                }
    }
}
