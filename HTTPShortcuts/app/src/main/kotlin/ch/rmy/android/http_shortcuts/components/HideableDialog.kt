package ch.rmy.android.http_shortcuts.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import ch.rmy.android.http_shortcuts.R

@Composable
fun HideableDialog(
    title: String? = null,
    message: String,
    onHidden: (Boolean) -> Unit,
    onDismissed: () -> Unit,
) {
    var permanentlyHidden by remember {
        mutableStateOf(false)
    }
    AlertDialog(
        onDismissRequest = onDismissed,
        title = title?.let {
            {
                Text(it)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.MEDIUM),
            ) {
                Text(message)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = permanentlyHidden,
                            onValueChange = {
                                permanentlyHidden = !permanentlyHidden
                                onHidden(permanentlyHidden)
                            },
                        )
                        .padding(Spacing.TINY),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.SMALL, Alignment.Start),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    androidx.compose.material3.Checkbox(
                        checked = permanentlyHidden,
                        onCheckedChange = null,
                    )
                    Text(
                        stringResource(R.string.dialog_checkbox_do_not_show_again),
                        fontSize = FontSize.MEDIUM,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissed) {
                Text(stringResource(R.string.dialog_ok))
            }
        },
    )
}
