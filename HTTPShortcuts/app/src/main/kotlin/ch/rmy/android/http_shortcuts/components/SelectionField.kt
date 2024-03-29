package ch.rmy.android.http_shortcuts.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import ch.rmy.android.http_shortcuts.extensions.runIf

@Composable
fun <T> SelectionField(
    title: String,
    selectedKey: T,
    items: List<Pair<T, String>>,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var expanded by remember(enabled) { mutableStateOf(false) }
    var dropdownWidth by remember { mutableIntStateOf(0) }

    Box(
        Modifier
            .fillMaxWidth()
            .then(modifier)
            .onGloballyPositioned { layoutCoordinates ->
                dropdownWidth = layoutCoordinates.size.width
            }
    ) {
        TextField(
            modifier = Modifier
                .fillMaxWidth(),
            enabled = enabled,
            label = {
                Text(title)
            },
            value = items.find { it.first == selectedKey }?.second ?: "",
            onValueChange = {},
            interactionSource = clickOnlyInteractionSource {
                if (enabled) {
                    expanded = !expanded
                }
            },
            readOnly = true,
            trailingIcon = {
                Icon(
                    Icons.Outlined.ArrowDropDown, null,
                    modifier = Modifier
                        .padding(Spacing.SMALL)
                        .runIf(!enabled) {
                            alpha(0.4f)
                        },
                )
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(with(LocalDensity.current) { dropdownWidth.toDp() })
        ) {
            items.forEach { (key, value) ->
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        onItemSelected(key)
                    },
                    text = {
                        Text(
                            text = value,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Start)
                        )
                    },
                )
            }
        }
    }
}
