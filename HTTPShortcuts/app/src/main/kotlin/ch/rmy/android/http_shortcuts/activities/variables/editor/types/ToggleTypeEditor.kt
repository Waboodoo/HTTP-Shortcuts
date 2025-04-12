package ch.rmy.android.http_shortcuts.activities.variables.editor.types

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import ch.rmy.android.framework.extensions.move
import ch.rmy.android.framework.extensions.swapped
import ch.rmy.android.framework.utils.UUIDUtils
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.FontSize
import ch.rmy.android.http_shortcuts.components.SettingsGroup
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.components.VariablePlaceholderText
import ch.rmy.android.http_shortcuts.components.VariablePlaceholderTextField
import ch.rmy.android.http_shortcuts.components.VerticalSpacer
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun ToggleTypeEditor(
    savedStateHandle: SavedStateHandle,
    viewState: ToggleTypeViewState,
    onViewStateChanged: (ToggleTypeViewState) -> Unit,
) {
    val currentViewState by rememberUpdatedState(viewState)

    var dialogVisible by rememberSaveable(key = "toggle-dialog-visible") {
        mutableStateOf(false)
    }
    var dialogOptionId by rememberSaveable(key = "toggle-dialog-option-id") {
        mutableStateOf<String?>(null)
    }
    var dialogOptionValue by rememberSaveable(key = "toggle-dialog-option-value") {
        mutableStateOf("")
    }

    SettingsGroup(
        title = stringResource(R.string.section_toggle_options),
    ) {
        OptionsList(
            options = viewState.options,
            onOptionClicked = { optionItem ->
                dialogOptionId = optionItem.id
                dialogOptionValue = optionItem.text
                dialogVisible = true
            },
            onOptionMoved = { optionId1, optionId2 ->
                onViewStateChanged(
                    currentViewState.copy(
                        options = currentViewState.options.swapped(optionId1, optionId2) { id },
                    ),
                )
            },
        )

        if (viewState.tooFewOptionsError) {
            Text(
                modifier = Modifier.padding(horizontal = Spacing.MEDIUM),
                text = stringResource(R.string.error_not_enough_toggle_values),
                fontSize = FontSize.SMALL,
                style = TextStyle(
                    color = Color.Red,
                ),
            )
        }

        if (viewState.options.isNotEmpty() || viewState.tooFewOptionsError) {
            VerticalSpacer(Spacing.SMALL)
        }

        Button(
            modifier = Modifier.padding(horizontal = Spacing.MEDIUM),
            onClick = {
                dialogOptionId = null
                dialogOptionValue = ""
                dialogVisible = true
            },
        ) {
            Text(stringResource(R.string.button_add_toggle_option))
        }
    }

    if (dialogVisible) {
        EditDialog(
            savedStateHandle = savedStateHandle,
            isEdit = dialogOptionId != null,
            value = dialogOptionValue,
            onValueChanged = {
                dialogOptionValue = it
            },
            onConfirm = {
                onViewStateChanged(
                    currentViewState.copy(
                        options = if (dialogOptionId != null) {
                            currentViewState.options.map {
                                if (it.id == dialogOptionId) {
                                    it.copy(text = dialogOptionValue)
                                } else {
                                    it
                                }
                            }
                        } else {
                            currentViewState.options.plus(
                                ToggleTypeViewState.OptionItem(id = UUIDUtils.newUUID(), text = dialogOptionValue),
                            )
                        },
                        tooFewOptionsError = false,
                    ),
                )
                dialogVisible = false
            },
            onDelete = {
                onViewStateChanged(
                    currentViewState.copy(
                        options = currentViewState.options.filter {
                            it.id != dialogOptionId
                        },
                        tooFewOptionsError = false,
                    ),
                )
                dialogVisible = false
            },
            onDismiss = {
                dialogVisible = false
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun OptionsList(
    options: List<ToggleTypeViewState.OptionItem>,
    onOptionClicked: (ToggleTypeViewState.OptionItem) -> Unit,
    onOptionMoved: (String, String) -> Unit,
) {
    var localOptions by remember(options) { mutableStateOf(options) }
    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        localOptions = localOptions.move(from.index, to.index)
        onOptionMoved(from.key as String, to.key as String)
    }

    if (options.isNotEmpty()) {
        HorizontalDivider()
    }
    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxWidth()
            .sizeIn(maxHeight = 3000.dp),
    ) {
        items(
            items = localOptions,
            key = { it.id },
        ) { item ->
            ReorderableItem(reorderableState, key = item.id) { isDragging ->
                val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp)
                OptionListItem(
                    option = item,
                    modifier = Modifier
                        .shadow(elevation.value)
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable {
                            onOptionClicked(item)
                        }
                        .longPressDraggableHandle(),
                )
            }
        }
    }
}

@Composable
private fun OptionListItem(
    option: ToggleTypeViewState.OptionItem,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier,
    ) {
        ListItem(
            modifier = Modifier
                .fillMaxWidth(),
            headlineContent = {
                VariablePlaceholderText(
                    option.text.ifEmpty { stringResource(R.string.empty_option_placeholder) },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
        )
        HorizontalDivider()
    }
}

@Composable
private fun EditDialog(
    savedStateHandle: SavedStateHandle,
    isEdit: Boolean,
    value: String,
    onValueChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(if (isEdit) R.string.title_edit_toggle_option else R.string.title_add_toggle_option))
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.SMALL),
            ) {
                VariablePlaceholderTextField(
                    savedStateHandle = savedStateHandle,
                    modifier = Modifier
                        .fillMaxWidth(),
                    key = "toggle-option-text",
                    allowOpeningVariableEditor = false,
                    value = value,
                    label = {
                        Text(stringResource(R.string.label_select_value))
                    },
                    onValueChange = onValueChanged,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Go,
                    ),
                    keyboardActions = KeyboardActions {
                        onConfirm()
                    },
                    textStyle = TextStyle(
                        fontSize = FontSize.SMALL,
                    ),
                    maxLines = 5,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
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
