package ch.rmy.android.http_shortcuts.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun ProgressDialog(
    text: String? = null,
    progress: Float? = null,
    onDismissRequest: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnClickOutside = false,
        ),
    ) {
        Surface(
            shape = AlertDialogDefaults.shape,
            tonalElevation = AlertDialogDefaults.TonalElevation,
            color = AlertDialogDefaults.containerColor,
        ) {
            Row(
                modifier = Modifier
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(Spacing.MEDIUM),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (progress != null) {
                    val smoothProgress by animateFloatAsState(progress)
                    CircularProgressIndicator(
                        progress = { smoothProgress },
                    )
                } else {
                    CircularProgressIndicator()
                }
                if (text != null) {
                    Text(text)
                }
            }
        }
    }
}
