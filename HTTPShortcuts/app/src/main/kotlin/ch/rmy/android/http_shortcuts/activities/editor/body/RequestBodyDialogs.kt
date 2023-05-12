package ch.rmy.android.http_shortcuts.activities.editor.body

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.FontSize
import ch.rmy.android.http_shortcuts.components.SelectDialog
import ch.rmy.android.http_shortcuts.components.SelectDialogEntry
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.components.VariablePlaceholderTextField
import ch.rmy.android.http_shortcuts.data.enums.ParameterType

@Composable
fun RequestBodyDialogs(
    dialogState: RequestBodyDialogState?,
    onParameterTypeSelected: (ParameterType) -> Unit,
    onParameterEdited: (key: String, value: String, fileName: String) -> Unit,
    onParameterDeleted: () -> Unit,
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
                isEdit = dialogState.id != null,
                type = dialogState.type,
                initialKey = dialogState.key,
                initialValue = dialogState.value,
                initialFileName = dialogState.fileName,
                onConfirmed = onParameterEdited,
                onDelete = onParameterDeleted,
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
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            SelectDialogEntry(
                label = stringResource(R.string.option_parameter_type_string),
                onClick = {
                    onParameterTypeSelected(ParameterType.STRING)
                },
            )
            SelectDialogEntry(
                label = stringResource(R.string.option_parameter_type_image),
                onClick = {
                    onParameterTypeSelected(ParameterType.IMAGE)
                },
            )
            SelectDialogEntry(
                label = stringResource(R.string.option_parameter_type_file),
                onClick = {
                    onParameterTypeSelected(ParameterType.FILE)
                },
            )
            SelectDialogEntry(
                label = stringResource(R.string.option_parameter_type_files),
                onClick = {
                    onParameterTypeSelected(ParameterType.FILES)
                },
            )
        }
    }
}

@Composable
private fun EditParameterDialog(
    isEdit: Boolean,
    type: ParameterType,
    initialKey: String = "",
    initialValue: String = "",
    initialFileName: String,
    onConfirmed: (key: String, value: String, fileName: String) -> Unit,
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

    AlertDialog(
        onDismissRequest = onDismissed,
        title = {
            Text(
                stringResource(
                    when (type) {
                        ParameterType.STRING -> if (isEdit) R.string.title_post_param_edit else R.string.title_post_param_add
                        ParameterType.FILE -> if (isEdit) R.string.title_post_param_edit_file else R.string.title_post_param_add_file
                        ParameterType.FILES -> if (isEdit) R.string.title_post_param_edit_file else R.string.title_post_param_add_files
                        ParameterType.IMAGE -> if (isEdit) R.string.title_post_param_edit_image else R.string.title_post_param_add_image
                    }
                ),
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.SMALL),
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    VariablePlaceholderTextField(
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

                if (type == ParameterType.FILE || type == ParameterType.IMAGE) {
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth(),
                        value = fileName,
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
            }
        },
        confirmButton = {
            TextButton(
                enabled = key.isNotEmpty(),
                onClick = {
                    onConfirmed(key, value, fileName)
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
