package ch.rmy.android.http_shortcuts.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import ch.rmy.android.http_shortcuts.R

private const val CHANGELOG_ASSET_URL = "file:///android_asset/changelog.html"

@Composable
fun ChangeLogDialog(
    permanentlyHidden: Boolean,
    onPermanentlyHiddenChanged: (Boolean) -> Unit,
    onDismissRequested: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequested,
        title = {
            Text(stringResource(R.string.changelog_title))
        },
        text = {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(Spacing.SMALL),
            ) {
                SinglePageBrowser(
                    CHANGELOG_ASSET_URL,
                    modifier = Modifier.weight(1f, fill = true)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onPermanentlyHiddenChanged(!permanentlyHidden)
                        }
                        .padding(Spacing.SMALL),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.SMALL, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = permanentlyHidden,
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
