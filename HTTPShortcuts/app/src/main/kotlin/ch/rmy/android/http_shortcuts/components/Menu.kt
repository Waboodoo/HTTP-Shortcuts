package ch.rmy.android.http_shortcuts.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R

@Composable
fun Menu(
    content: @Composable MenuScope.() -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ToolbarIcon(
        Icons.Outlined.MoreVert,
        contentDescription = stringResource(R.string.accessibility_main_menu),
        onClick = { expanded = !expanded },
    )
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
    ) {
        with(object : MenuScope {
            override fun onItemSelected() {
                expanded = false
            }
        }) {
            content()
        }
    }
}

interface MenuScope {
    fun onItemSelected()
}

@Composable
fun MenuScope.MenuItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(title) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
            )
        },
        onClick = {
            onClick()
            onItemSelected()
        },
    )
}
