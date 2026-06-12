package ch.rmy.android.http_shortcuts.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import ch.rmy.android.http_shortcuts.extensions.runIf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectDialog(
    title: String? = null,
    extraButton: (@Composable () -> Unit)? = null,
    onDismissRequest: () -> Unit,
    scrolling: Boolean = true,
    content: @Composable ColumnScope.(horizontalPadding: Dp) -> Unit,
) {
    if (extraButton != null) {
        AlertDialog(
            modifier = Modifier.padding(Spacing.MEDIUM),
            properties = DialogProperties(usePlatformDefaultWidth = false),
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
                    content(0.dp)
                }
            },
            confirmButton = {},
            dismissButton = extraButton,
        )
    } else {
        BasicAlertDialog(
            modifier = Modifier.padding(Spacing.MEDIUM),
            properties = DialogProperties(usePlatformDefaultWidth = false),
            onDismissRequest = {
                onDismissRequest()
            },
        ) {
            Surface(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.large,
                color = AlertDialogDefaults.containerColor,
                contentColor = AlertDialogDefaults.textContentColor,
                tonalElevation = AlertDialogDefaults.TonalElevation,
            ) {
                val horizontalPadding = Spacing.MEDIUM + Spacing.SMALL
                Column(
                    modifier = Modifier.padding(vertical = Spacing.MEDIUM),
                ) {
                    if (!title.isNullOrEmpty()) {
                        Text(
                            modifier = Modifier.padding(
                                horizontal = horizontalPadding,
                                vertical = Spacing.SMALL,
                            ),
                            text = title,
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    }
                    Column(
                        modifier = Modifier
                            .runIf(scrolling) {
                                verticalScroll(rememberScrollState())
                            },
                    ) {
                        content(horizontalPadding)
                    }
                }
            }
        }
    }
}
