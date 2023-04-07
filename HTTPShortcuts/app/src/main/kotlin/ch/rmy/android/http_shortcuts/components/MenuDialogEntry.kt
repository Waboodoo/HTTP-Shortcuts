package ch.rmy.android.http_shortcuts.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

@Composable
fun MenuDialogEntry(
    label: String,
    checked: Boolean? = null,
    icon: ShortcutIcon? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.SMALL),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (checked != null) {
            Checkbox(
                checked = checked,
                onCheckedChange = null,
                modifier = Modifier
                    .padding(end = Spacing.SMALL)
            )
        }
        if (icon != null) {
            Icon(
                icon,
                modifier = Modifier
                    .padding(end = Spacing.SMALL)
            )
        }
        Text(
            label,
            fontSize = FontSize.BIG,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
