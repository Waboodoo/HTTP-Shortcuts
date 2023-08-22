package ch.rmy.android.http_shortcuts.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role

@Composable
fun Checkbox(
    modifier: Modifier = Modifier,
    checked: Boolean,
    enabled: Boolean = true,
    icon: @Composable (() -> Unit)? = null,
    label: String,
    subtitle: String? = null,
    onCheckedChange: (Boolean) -> Unit = {},
) {
    Surface {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .toggleable(
                    enabled = enabled,
                    value = checked,
                    role = Role.Checkbox,
                    onValueChange = { onCheckedChange(!checked) }
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ListItem(
                headlineContent = {
                    WithContentColor(enabled = enabled) {
                        Text(label)
                    }
                },
                supportingContent = subtitle?.let {
                    {
                        WithContentColor(enabled = enabled) {
                            Text(it)
                        }
                    }
                },
                leadingContent = icon?.let {
                    {
                        WithContentColor(enabled = enabled) {
                            icon()
                        }
                    }
                },
                trailingContent = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.Checkbox(
                            enabled = enabled,
                            checked = checked,
                            onCheckedChange = null,
                            modifier = Modifier.minimumInteractiveComponentSize(),
                        )
                    }
                },
            )
        }
    }
}

@Composable
private fun WithContentColor(
    enabled: Boolean,
    content: @Composable () -> Unit,
) {
    val color = LocalContentColor.current.copy(alpha = if (enabled) 1.0f else 0.6f)
    CompositionLocalProvider(LocalContentColor provides color) {
        content()
    }
}
