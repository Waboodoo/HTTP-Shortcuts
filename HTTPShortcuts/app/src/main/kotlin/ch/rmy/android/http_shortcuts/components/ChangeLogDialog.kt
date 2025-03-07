package ch.rmy.android.http_shortcuts.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import ch.rmy.android.http_shortcuts.R

private const val CHANGELOG_ASSET_URL = "file:///android_asset/changelog.html"

@Composable
fun ChangeLogDialog(
    title: String = stringResource(R.string.changelog_title),
    permanentlyHidden: Boolean,
    onPermanentlyHiddenChanged: (Boolean) -> Unit,
    onDismissRequested: () -> Unit,
) {
    var hiddenState by remember {
        mutableStateOf(permanentlyHidden)
    }
    AlertDialog(
        onDismissRequest = onDismissRequested,
        title = {
            Text(title)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(Spacing.SMALL),
            ) {
                SinglePageBrowser(
                    CHANGELOG_ASSET_URL,
                    modifier = Modifier.weight(1f, fill = true),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics(mergeDescendants = true) {}
                        .toggleable(
                            value = !hiddenState,
                            role = Role.Checkbox,
                            onValueChange = {
                                hiddenState = !hiddenState
                                onPermanentlyHiddenChanged(hiddenState)
                            },
                        )
                        .padding(Spacing.TINY),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.SMALL, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = !hiddenState,
                        onCheckedChange = null,
                    )
                    Text(
                        stringResource(R.string.changelog_checkbox_show_at_startup),
                        fontSize = FontSize.MEDIUM,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismissRequested,
            ) {
                Text(stringResource(R.string.dialog_ok))
            }
        },
    )
}
