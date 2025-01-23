package ch.rmy.android.http_shortcuts.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable

@Composable
fun FloatingAddButton(
    onClick: () -> Unit,
    contentDescription: String,
) {
    FloatingActionButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Outlined.Add,
            contentDescription = contentDescription,
        )
    }
}
