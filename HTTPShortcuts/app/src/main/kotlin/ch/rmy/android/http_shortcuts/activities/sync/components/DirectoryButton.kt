package ch.rmy.android.http_shortcuts.activities.sync.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.clickOnlyInteractionSource

@Composable
fun DirectoryButton(directoryName: String, onClick: () -> Unit) {
    val directoryLabel = stringResource(R.string.label_sync_directory)
    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .clearAndSetSemantics {
                contentDescription = "$directoryLabel: $directoryName"
                role = Role.Button
                onClick {
                    onClick()
                    true
                }
            },
        label = {
            Text(
                text = directoryLabel,
            )
        },
        value = directoryName,
        onValueChange = {},
        interactionSource = clickOnlyInteractionSource {
            onClick()
        },
        singleLine = true,
        readOnly = true,
    )
}
