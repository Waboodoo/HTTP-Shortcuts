package ch.rmy.android.http_shortcuts.activities.editor.body

import android.app.Application
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.swapped
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.editor.body.models.ParameterListItem
import ch.rmy.android.http_shortcuts.activities.editor.body.usecases.GetFileParameterDialogUseCase
import ch.rmy.android.http_shortcuts.activities.variables.VariablesActivity
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.enums.ParameterType
import ch.rmy.android.http_shortcuts.data.enums.RequestBodyType
import ch.rmy.android.http_shortcuts.data.models.ParameterModel
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.usecases.GetKeyValueDialogUseCase
import ch.rmy.android.http_shortcuts.usecases.GetVariablePlaceholderPickerDialogUseCase
import ch.rmy.android.http_shortcuts.usecases.KeepVariablePlaceholderProviderUpdatedUseCase
import javax.inject.Inject

class RequestBodyViewModel(application: Application) : BaseViewModel<Unit, RequestBodyViewState>(application), WithDialog {

    @Inject
    lateinit var temporaryShortcutRepository: TemporaryShortcutRepository

    @Inject
    lateinit var keepVariablePlaceholderProviderUpdated: KeepVariablePlaceholderProviderUpdatedUseCase

    @Inject
    lateinit var getFileParameterDialog: GetFileParameterDialogUseCase

    @Inject
    lateinit var getKeyValueDialog: GetKeyValueDialogUseCase

    @Inject
    lateinit var getVariablePlaceholderPickerDialog: GetVariablePlaceholderPickerDialogUseCase

    init {
        getApplicationComponent().inject(this)
    }

    private var parameters: List<ParameterModel> = emptyList()
        set(value) {
            field = value
            updateViewState {
                copy(
                    parameters = mapParameters(value),
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

    override fun initViewState() = RequestBodyViewState()

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

    private fun onAddStringParameterDialogConfirmed(key: String, value: String) {
        temporaryShortcutRepository.addStringParameter(key, value)
            .compose(progressMonitor.singleTransformer())
            .subscribe { newParameter ->
                parameters = parameters.plus(newParameter)
            }
            .attachTo(destroyer)
    }

    private fun onAddFileParameterDialogConfirmed(key: String, fileName: String, multiple: Boolean, image: Boolean) {
        temporaryShortcutRepository.addFileParameter(key, fileName, multiple, image)
            .compose(progressMonitor.singleTransformer())
            .subscribe { newParameter ->
                parameters = parameters.plus(newParameter)
            }
            .attachTo(destroyer)
    }

    private fun onEditParameterDialogConfirmed(parameterId: String, key: String, value: String = "", fileName: String = "") {
        parameters = parameters
            .map { parameter ->
                if (parameter.id == parameterId) {
                    ParameterModel(
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
        performOperation(
            temporaryShortcutRepository.updateParameter(parameterId, key, value, fileName)
        )
    }

    private fun onRemoveParameterButtonClicked(parameterId: String) {
        parameters = parameters
            .filter { parameter ->
                parameter.id != parameterId
            }
        performOperation(
            temporaryShortcutRepository.removeParameter(parameterId)
        )
    }

    fun onAddParameterButtonClicked() {
        doWithViewState { viewState ->
            if (viewState.requestBodyType == RequestBodyType.FORM_DATA) {
                showParameterTypeDialog()
            } else {
                showAddParameterDialogForString()
            }
        }
    }

    private fun showParameterDialog(
        title: Localizable,
        showRemoveOption: Boolean = false,
        showFileNameOption: Boolean = false,
        keyName: String = "",
        fileName: String = "",
        onConfirm: (keyName: String, fileName: String) -> Unit,
        onRemove: () -> Unit = {},
    ) {
        dialogState = getFileParameterDialog(
            title = title,
            showRemoveOption = showRemoveOption,
            showFileNameOption = showFileNameOption,
            keyName = keyName,
            fileName = fileName,
            onConfirm = onConfirm,
            onRemove = onRemove,
        )
    }

    private fun showKeyValueDialog(
        title: Localizable,
        keyLabel: Localizable,
        valueLabel: Localizable,
        key: String? = null,
        value: String? = null,
        onConfirm: (key: String, value: String) -> Unit,
        onRemove: () -> Unit = {},
    ) {
        dialogState = getKeyValueDialog(
            title = title,
            keyLabel = keyLabel,
            valueLabel = valueLabel,
            key = key,
            value = value,
            isMultiLine = true,
            onConfirm = onConfirm,
            onRemove = onRemove,
        )
    }

    private fun showParameterTypeDialog() {
        dialogState = DialogState.create {
            title(R.string.dialog_title_parameter_type)
                .item(R.string.option_parameter_type_string, action = ::showAddParameterDialogForString)
                .item(R.string.option_parameter_type_image) {
                    showAddParameterDialogForFile(image = true)
                }
                .item(R.string.option_parameter_type_file) {
                    showAddParameterDialogForFile()
                }
                .item(R.string.option_parameter_type_files) {
                    showAddParameterDialogForFile(multiple = true)
                }
                .build()
        }
    }

    private fun showAddParameterDialogForString() {
        showKeyValueDialog(
            title = StringResLocalizable(R.string.title_post_param_add),
            keyLabel = StringResLocalizable(R.string.label_post_param_key),
            valueLabel = StringResLocalizable(R.string.label_post_param_value),
            onConfirm = { key: String, value: String ->
                onAddStringParameterDialogConfirmed(key, value)
            },
        )
    }

    private fun showAddParameterDialogForFile(multiple: Boolean = false, image: Boolean = false) {
        showParameterDialog(
            title = StringResLocalizable(
                when {
                    image -> R.string.title_post_param_add_image
                    multiple -> R.string.title_post_param_add_files
                    else -> R.string.title_post_param_add_file
                }
            ),
            showFileNameOption = !multiple,
            onConfirm = { keyName: String, fileName: String ->
                onAddFileParameterDialogConfirmed(key = keyName, fileName = fileName, multiple = multiple, image = image)
            },
        )
    }

    fun onParameterClicked(id: String) {
        parameters.firstOrNull { parameter ->
            parameter.id == id
        }
            ?.let { parameter ->
                when (parameter.parameterType) {
                    ParameterType.STRING -> {
                        showEditParameterDialogForString(
                            id,
                            parameter.key,
                            parameter.value,
                        )
                    }
                    ParameterType.FILE -> {
                        showEditParameterDialogForFile(
                            id,
                            parameter.key,
                            showFileNameOption = true,
                            fileName = parameter.fileName,
                        )
                    }
                    ParameterType.IMAGE,
                    -> {
                        showEditParameterDialogForFile(
                            id,
                            parameter.key,
                            showFileNameOption = true,
                            fileName = parameter.fileName,
                            image = true,
                        )
                    }
                    ParameterType.FILES -> {
                        showEditParameterDialogForFile(
                            id,
                            parameter.key,
                            showFileNameOption = false,
                            fileName = parameter.fileName,
                        )
                    }
                }
            }
    }

    private fun showEditParameterDialogForString(
        parameterId: String,
        parameterKey: String,
        value: String,
    ) {
        showKeyValueDialog(
            title = StringResLocalizable(R.string.title_post_param_edit),
            keyLabel = StringResLocalizable(R.string.label_post_param_key),
            valueLabel = StringResLocalizable(R.string.label_post_param_value),
            key = parameterKey,
            value = value,
            onConfirm = { newKey: String, newValue: String ->
                onEditParameterDialogConfirmed(parameterId, newKey, newValue)
            },
            onRemove = {
                onRemoveParameterButtonClicked(parameterId)
            },
        )
    }

    private fun showEditParameterDialogForFile(
        parameterId: String,
        parameterKey: String,
        showFileNameOption: Boolean,
        fileName: String,
        image: Boolean = false,
    ) {
        showParameterDialog(
            title = StringResLocalizable(if (image) R.string.title_post_param_edit_image else R.string.title_post_param_edit_file),
            showRemoveOption = true,
            showFileNameOption = showFileNameOption,
            keyName = parameterKey,
            fileName = fileName,
            onConfirm = { newKey: String, newFileName: String ->
                onEditParameterDialogConfirmed(parameterId = parameterId, key = newKey, fileName = newFileName)
            },
            onRemove = {
                onRemoveParameterButtonClicked(parameterId)
            }
        )
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
        doWithViewState { viewState ->
            if (viewState.contentType.isEmpty() && bodyContent.isJsonObjectStart()) {
                onContentTypeChanged("application/json")
            }
            updateViewState {
                copy(bodyContent = bodyContent)
            }
            performOperation(
                temporaryShortcutRepository.setBodyContent(bodyContent)
            )
        }
    }

    fun onBodyContentVariableButtonClicked() {
        dialogState = getVariablePlaceholderPickerDialog.invoke(
            onVariableSelected = {
                emitEvent(RequestBodyEvent.InsertVariablePlaceholder(it))
            },
            onEditVariableButtonClicked = {
                openActivity(
                    VariablesActivity.IntentBuilder()
                )
            },
        )
    }

    fun onBackPressed() {
        waitForOperationsToFinish {
            finish()
        }
    }

    companion object {
        private fun mapParameters(parameters: List<ParameterModel>): List<ParameterListItem> =
            parameters.map { parameter ->
                ParameterListItem.Parameter(
                    id = parameter.id,
                    key = parameter.key,
                    value = parameter.value.takeIf { parameter.isStringParameter },
                    label = when (parameter.parameterType) {
                        ParameterType.FILE -> StringResLocalizable(R.string.subtitle_parameter_value_file)
                        ParameterType.FILES -> StringResLocalizable(R.string.subtitle_parameter_value_files)
                        ParameterType.IMAGE -> StringResLocalizable(R.string.subtitle_parameter_value_image)
                        ParameterType.STRING -> null
                    },
                )
            }
                .ifEmpty {
                    listOf(ParameterListItem.EmptyState)
                }

        private val JSON_OBJECT_START = "^\\s*\\{\\s*\".*".toRegex()

        private fun String.isJsonObjectStart() =
            matches(JSON_OBJECT_START)
    }
}
