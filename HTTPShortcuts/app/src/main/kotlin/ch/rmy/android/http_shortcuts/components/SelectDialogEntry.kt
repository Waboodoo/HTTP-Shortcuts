package ch.rmy.android.http_shortcuts.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import ch.rmy.android.http_shortcuts.extensions.runIf
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

@Composable
fun SelectDialogEntry(
    label: String,
    description: String? = null,
    checked: Boolean? = null,
    enabled: Boolean = true,
    useRadios: Boolean = false,
    shortcutIcon: ShortcutIcon? = null,
    icon: ImageVector? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {}
            .run {
                if (useRadios) {
                    clickable(
                        enabled = enabled,
                        role = Role.RadioButton,
                        onClick = onClick,
                    )
                } else if (checked != null) {
                    toggleable(
                        value = checked,
                        enabled = enabled,
                        role = Role.Checkbox,
                        onValueChange = {
                            onClick()
                        },
                    )
                } else {
                    clickable(
                        enabled = enabled,
                        onClick = onClick,
                    )
                }
            }
            .padding(vertical = Spacing.SMALL + Spacing.TINY),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (checked != null) {
            if (useRadios) {
                RadioButton(
                    selected = checked,
                    enabled = enabled,
                    onClick = null,
                    modifier = Modifier.padding(end = Spacing.SMALL),
                )
            } else {
                Checkbox(
                    checked = checked,
                    enabled = enabled,
                    onCheckedChange = null,
                    modifier = Modifier.padding(end = Spacing.SMALL),
                )
            }
        }
        if (shortcutIcon != null) {
            ShortcutIcon(
                shortcutIcon,
                modifier = Modifier
                    .padding(end = Spacing.SMALL + Spacing.TINY)
                    .runIf(!enabled) {
                        alpha(0.3f)
                    },
            )
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .padding(end = Spacing.SMALL + Spacing.TINY)
                    .runIf(!enabled) {
                        alpha(0.3f)
                    },
            )
        }
        Column(
            modifier = Modifier
                .runIf(!enabled) {
                    alpha(0.3f)
                },
        ) {
            Text(
                label,
                fontSize = FontSize.BIG,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
            )
            if (description != null) {
                Text(
                    description,
                    fontSize = FontSize.SMALL,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
