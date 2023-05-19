package ch.rmy.android.http_shortcuts.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ch.rmy.android.http_shortcuts.extensions.runIf

@Composable
fun SelectDialog(
    title: String? = null,
    extraButton: (@Composable () -> Unit)? = null,
    onDismissRequest: () -> Unit,
    scrolling: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    AlertDialog(
        onDismissRequest = {
            onDismissRequest()
        },
        title = title?.let {
            {
                Text(title)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .runIf(scrolling) {
                        verticalScroll(rememberScrollState())
                    },
            ) {
                content()
            }
        },
        confirmButton = {},
        dismissButton = extraButton,
    )
}
