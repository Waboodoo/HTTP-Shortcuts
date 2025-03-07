package ch.rmy.android.http_shortcuts.activities.categories.sections

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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.FontSize
import ch.rmy.android.http_shortcuts.components.Spacing

@Composable
fun CategorySectionsDialogs(
    dialogState: CategorySectionsDialogState?,
    onConfirmed: (name: String) -> Unit,
    onDelete: () -> Unit,
    onDismissed: () -> Unit,
) {
    when (dialogState) {
        is CategorySectionsDialogState.AddSection -> {
            EditSectionDialog(
                isEdit = false,
                onConfirmed = onConfirmed,
                onDismissed = onDismissed,
            )
        }
        is CategorySectionsDialogState.EditSection -> {
            EditSectionDialog(
                isEdit = true,
                initialName = dialogState.name,
                onConfirmed = onConfirmed,
                onDelete = onDelete,
                onDismissed = onDismissed,
            )
        }
        null -> Unit
    }
}

@Composable
private fun EditSectionDialog(
    isEdit: Boolean,
    initialName: String = "",
    onConfirmed: (name: String) -> Unit,
    onDelete: () -> Unit = {},
    onDismissed: () -> Unit,
) {
    var name by rememberSaveable(key = "edit-section-name") {
        mutableStateOf(initialName)
    }

    AlertDialog(
        onDismissRequest = onDismissed,
        title = {
            Text(stringResource(if (isEdit) R.string.title_category_section_edit else R.string.title_category_section_add))
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.SMALL),
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    var hasFocus by remember {
                        mutableStateOf(false)
                    }
                    TextField(
                        modifier = Modifier
                            .onFocusChanged {
                                hasFocus = it.isFocused
                            }
                            .fillMaxWidth(),
                        value = name,
                        label = {
                            Text(stringResource(R.string.label_category_section_name))
                        },
                        onValueChange = {
                            name = it
                        },
                        textStyle = TextStyle(
                            fontSize = FontSize.SMALL,
                        ),
                        maxLines = 4,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    onConfirmed(name)
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
