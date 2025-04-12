package ch.rmy.android.http_shortcuts.activities.editor.body

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.lifecycle.SavedStateHandle
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.FontSize
import ch.rmy.android.http_shortcuts.components.SelectDialog
import ch.rmy.android.http_shortcuts.components.SelectDialogEntry
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.components.VariablePlaceholderTextField
import ch.rmy.android.http_shortcuts.components.VerticalSpacer
import ch.rmy.android.http_shortcuts.data.enums.FileUploadType
import ch.rmy.android.http_shortcuts.data.enums.ParameterType

@Composable
fun RequestBodyDialogs(
    dialogState: RequestBodyDialogState?,
    savedStateHandle: SavedStateHandle,
    onParameterTypeSelected: (ParameterType) -> Unit,
    onParameterEdited: (key: String, value: String, fileName: String, useImageEditor: Boolean) -> Unit,
    onParameterDeleted: () -> Unit,
    onFileUploadTypeChanged: (FileUploadType) -> Unit,
    onSourceFileNameClicked: () -> Unit,
    onDismissed: () -> Unit,
) {
    when (dialogState) {
        is RequestBodyDialogState.ParameterTypePicker -> {
            ParameterTypePickerDialog(
                onParameterTypeSelected = onParameterTypeSelected,
                onDismissed = onDismissed,
            )
        }
        is RequestBodyDialogState.ParameterEditor -> {
            EditParameterDialog(
                savedStateHandle = savedStateHandle,
                isEdit = dialogState.id != null,
                type = dialogState.type,
                fileUploadType = dialogState.fileUploadType,
                initialKey = dialogState.key,
                initialValue = dialogState.value,
                initialFileName = dialogState.fileName,
                initialUseImageEditor = dialogState.useImageEditor,
                sourceFileName = dialogState.sourceFileName,
                onConfirmed = onParameterEdited,
                onDelete = onParameterDeleted,
                onFileUploadTypeChanged = onFileUploadTypeChanged,
                onSourceFileNameClicked = onSourceFileNameClicked,
                onDismissed = onDismissed,
            )
        }
        null -> Unit
    }
}

@Composable
private fun ParameterTypePickerDialog(
    onParameterTypeSelected: (ParameterType) -> Unit,
    onDismissed: () -> Unit,
) {
    SelectDialog(
        title = stringResource(R.string.dialog_title_parameter_type),
        onDismissRequest = onDismissed,
    ) {
        SelectDialogEntry(
            label = stringResource(R.string.option_parameter_type_string),
            onClick = {
                onParameterTypeSelected(ParameterType.STRING)
            },
        )
        SelectDialogEntry(
            label = stringResource(R.string.option_parameter_type_file),
            onClick = {
                onParameterTypeSelected(ParameterType.FILE)
            },
        )
    }
}

@Composable
private fun EditParameterDialog(
    savedStateHandle: SavedStateHandle,
    isEdit: Boolean,
    type: ParameterType,
    fileUploadType: FileUploadType,
    initialKey: String = "",
    initialValue: String = "",
    initialFileName: String,
    initialUseImageEditor: Boolean,
    sourceFileName: String?,
    onConfirmed: (
        key: String,
        value: String,
        fileName: String,
        useImageEditor: Boolean,
    ) -> Unit,
    onFileUploadTypeChanged: (FileUploadType) -> Unit,
    onSourceFileNameClicked: () -> Unit,
    onDelete: () -> Unit = {},
    onDismissed: () -> Unit,
) {
    var key by rememberSaveable(key = "edit-parameter-key") {
        mutableStateOf(initialKey)
    }
    var value by rememberSaveable(key = "edit-parameter-value") {
        mutableStateOf(initialValue)
    }
    var fileName by rememberSaveable(key = "edit-parameter-filename") {
        mutableStateOf(initialFileName)
    }
    var useImageEditor by rememberSaveable(key = "edit-parameter-use-image-editor") {
        mutableStateOf(initialUseImageEditor)
    }

    AlertDialog(
        onDismissRequest = onDismissed,
        title = {
            Text(
                stringResource(
                    when (type) {
                        ParameterType.STRING -> if (isEdit) R.string.title_post_param_edit else R.string.title_post_param_add
                        ParameterType.FILE -> if (isEdit) R.string.title_post_param_edit_file else R.string.title_post_param_add_file
                    },
                ),
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.SMALL),
                modifier = Modifier.verticalScroll(rememberScrollState()),
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    VariablePlaceholderTextField(
                        savedStateHandle = savedStateHandle,
                        modifier = Modifier
                            .fillMaxWidth(),
                        key = "parameter-edit-key",
                        value = key,
                        label = {
                            Text(stringResource(R.string.label_post_param_key))
                        },
                        onValueChange = {
                            key = it
                        },
                        textStyle = TextStyle(
                            fontSize = FontSize.SMALL,
                        ),
                        maxLines = 10,
                    )
                }

                if (type == ParameterType.STRING) {
                    VariablePlaceholderTextField(
                        savedStateHandle = savedStateHandle,
                        modifier = Modifier
                            .fillMaxWidth(),
                        key = "parameter-edit-value",
                        value = value,
                        label = {
                            Text(stringResource(R.string.label_post_param_value))
                        },
                        onValueChange = {
                            value = it
                        },
                        textStyle = TextStyle(
                            fontSize = FontSize.SMALL,
                        ),
                        maxLines = 10,
                    )
                }

                if (type == ParameterType.FILE) {
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth(),
                        value = fileName,
                        enabled = fileUploadType != FileUploadType.FILE_PICKER_MULTI,
                        label = {
                            Text(stringResource(R.string.label_post_param_file_name))
                        },
                        onValueChange = {
                            fileName = it
                        },
                        textStyle = TextStyle(
                            fontSize = FontSize.SMALL,
                        ),
                        singleLine = true,
                    )
                }

                if (type == ParameterType.FILE) {
                    VerticalSpacer(Spacing.SMALL)
                    FileOptions(
                        allowMultiple = true,
                        useHorizontalPadding = false,
                        fileUploadType = fileUploadType,
                        fileName = sourceFileName,
                        useImageEditor = useImageEditor,
                        onFileUploadTypeChanged = onFileUploadTypeChanged,
                        onFileNameClicked = onSourceFileNameClicked,
                        onUseImageEditorChanged = {
                            useImageEditor = it
                        },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = key.isNotEmpty(),
                onClick = {
                    onConfirmed(key, value, fileName, useImageEditor)
                },
            ) {
                Text(stringResource(R.string.dialog_ok))
            }
        },
        dismissButton = {
            if (isEdit) {
                TextButton(
                    onClick = onDelete,
                ) {
                    Text(stringResource(R.string.dialog_remove))
                }
            }
        },
    )
}
