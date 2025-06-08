package ch.rmy.android.http_shortcuts.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.SavedStateHandle
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.variables.VariableTypeMappings.getTypeName
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKeyOrId
import ch.rmy.android.http_shortcuts.data.dtos.GlobalVariablePlaceholder
import ch.rmy.android.http_shortcuts.data.settings.Settings
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.navigation.ResultHandler
import ch.rmy.android.http_shortcuts.variables.Variables
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

@Composable
fun VariablePickerDialog(
    savedStateHandle: SavedStateHandle,
    title: String,
    globalVariables: List<GlobalVariablePlaceholder>,
    showEditButton: Boolean = true,
    onVariableSelected: (VariableKeyOrId) -> Unit,
    onDismissRequested: () -> Unit,
) {
    val context = LocalContext.current
    var showFirstTimeDialog by rememberSaveable {
        mutableStateOf(!Settings(context).isAwareOfVariablePlaceholders)
    }

    if (showFirstTimeDialog) {
        AlertDialog(
            onDismissRequest = {
                Settings(context).isAwareOfVariablePlaceholders = true
                onDismissRequested()
            },
            text = {
                Text(
                    stringResource(
                        if (showEditButton) {
                            R.string.help_text_variable_button
                        } else {
                            R.string.help_text_variable_button_for_variables
                        },
                    ),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        Settings(context).isAwareOfVariablePlaceholders = true
                        showFirstTimeDialog = false
                    },
                ) {
                    Text(stringResource(R.string.dialog_ok))
                }
            },
        )
        return
    }

    val eventHandler = LocalEventinator.current

    var pickerOpened by rememberSaveable {
        mutableStateOf(false)
    }
    if (pickerOpened) {
        ResultHandler(savedStateHandle) { result ->
            if (result is NavigationDestination.GlobalVariables.VariableSelectedResult) {
                onVariableSelected(VariableKeyOrId(result.globalVariableId))
                onDismissRequested()
                pickerOpened = false
            }
        }
    }

    var temporarilyHidden by remember {
        mutableStateOf(false)
    }

    val onEditVariablesClicked = {
        temporarilyHidden = true
        pickerOpened = true
        eventHandler.onEvent(ViewModelEvent.Navigate(NavigationDestination.GlobalVariables.buildRequest(asPicker = true)))
    }

    LaunchedEffect(temporarilyHidden) {
        if (temporarilyHidden) {
            delay(1.seconds)
            temporarilyHidden = false
        }
    }
    if (temporarilyHidden) {
        return
    }

    var localVariableDialogVisible by rememberSaveable {
        mutableStateOf(false)
    }

    if (localVariableDialogVisible) {
        TextInputDialog(
            title = stringResource(R.string.dialog_title_local_variable),
            message = stringResource(R.string.dialog_instructions_local_variable),
            allowEmpty = false,
            singleLine = true,
            transformValue = Variables::coerceToVariableKey,
            onDismissRequest = { value ->
                if (value != null) {
                    onVariableSelected(VariableKeyOrId(value))
                } else {
                    onDismissRequested()
                }
            },
        )
        return
    }

    SelectDialog(
        title = title,
        scrolling = false,
        onDismissRequest = onDismissRequested,
        extraButton = if (showEditButton) {
            {
                TextButton(onClick = onEditVariablesClicked) {
                    Text(stringResource(R.string.label_edit_variables))
                }
            }
        } else {
            null
        },
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            item(
                key = "local",
            ) {
                SelectDialogEntry(
                    label = stringResource(R.string.dialog_option_label_local_variable),
                    description = stringResource(R.string.dialog_option_subtitle_local_variable),
                    onClick = {
                        localVariableDialogVisible = true
                    },
                )
            }

            item(key = "divider") {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = Spacing.MEDIUM),
                )
            }

            if (globalVariables.isEmpty()) {
                item(
                    key = "no-global-variables",
                ) {
                    Text(
                        stringResource(R.string.help_text_no_global_variables_yet, stringResource(R.string.label_edit_variables)),
                    )
                }
            } else {
                items(
                    items = globalVariables,
                    key = { it.globalVariableId },
                ) { variable ->
                    SelectDialogEntry(
                        label = variable.variableKey,
                        description = stringResource(variable.variableType.getTypeName()),
                        onClick = {
                            onVariableSelected(VariableKeyOrId(variable.globalVariableId))
                        },
                    )
                }
            }
        }
    }
}
