package ch.rmy.android.http_shortcuts.components

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.rmy.android.http_shortcuts.R

@Composable
fun SettingsCheckbox(
    title: String,
    subtitle: String? = null,
    icon: Painter? = null,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChanged: (Boolean) -> Unit,
) {
    ListItem(
        leadingContent = icon?.let {
            { Icon(icon, contentDescription = null) }
        },
        headlineContent = { Text(title) },
        supportingContent = subtitle?.let {
            {
                Text(
                    text = it,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        colors = if (enabled) {
            ListItemDefaults.colors()
        } else {
            val disabledColor = ListItemDefaults.contentColor.copy(alpha = 0.38f)
            ListItemDefaults.colors(
                leadingIconColor = disabledColor,
                headlineColor = disabledColor,
                supportingColor = disabledColor,
                overlineColor = disabledColor,
                trailingIconColor = disabledColor,
            )
        },
        modifier = Modifier
            .toggleable(
                role = Role.Checkbox,
                enabled = enabled,
                value = checked,
                onValueChange = onCheckedChanged,
            )
            .heightIn(min = 72.dp)
            .padding(vertical = 4.dp),
        trailingContent = {
            Checkbox(
                enabled = enabled,
                checked = checked,
                onCheckedChange = null,
            )
        },
    )
}

@Preview
@Composable
private fun SettingsCheckbox_Checked_Preview() {
    SettingsCheckbox(
        icon = painterResource(R.drawable.outline_view_week_24),
        title = "Checkbox Title which may be a little long",
        checked = true,
        onCheckedChanged = {},
    )
}

@Preview
@Composable
private fun SettingsCheckbox_UncheckedWithSubtitle_Preview() {
    SettingsCheckbox(
        icon = painterResource(R.drawable.outline_move_down_24),
        title = "Checkbox Title",
        subtitle = "This one has a subtitle",
        checked = false,
        onCheckedChanged = {},
    )
}
