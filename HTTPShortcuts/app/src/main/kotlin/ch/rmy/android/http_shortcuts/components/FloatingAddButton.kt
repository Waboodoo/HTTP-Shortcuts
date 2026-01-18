package ch.rmy.android.http_shortcuts.components

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import ch.rmy.android.http_shortcuts.R

@Composable
fun FloatingAddButton(
    onClick: () -> Unit,
    contentDescription: String,
) {
    FloatingActionButton(onClick = onClick) {
        Icon(
            painter = painterResource(R.drawable.outline_add_24),
            contentDescription = contentDescription,
        )
    }
}
