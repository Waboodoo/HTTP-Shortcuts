package ch.rmy.android.http_shortcuts.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.unit.dp
import ch.rmy.android.http_shortcuts.extensions.runIf

@Composable
fun <T> SelectionField(
    title: String,
    selectedKey: T,
    items: List<Pair<T, String>>,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    itemFactory: @Composable (T, String) -> Unit = { _, value ->
        Text(
            text = value,
            modifier = Modifier.fillMaxWidth(),
        )
    },
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    var dropdownWidth by remember { mutableIntStateOf(0) }
    val selectedValue = items.find { it.first == selectedKey }?.second ?: ""

    LaunchedEffect(enabled) {
        if (!enabled) {
            expanded = false
        }
    }

    Box(
        Modifier
            .fillMaxWidth()
            .then(modifier)
            .onGloballyPositioned { layoutCoordinates ->
                dropdownWidth = layoutCoordinates.size.width
            },
    ) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .clearAndSetSemantics {
                    contentDescription = "$title: $selectedValue"
                    role = Role.DropdownList
                    onClick {
                        if (enabled) {
                            expanded = !expanded
                            true
                        } else {
                            false
                        }
                    }
                },
            enabled = enabled,
            label = {
                Text(title)
            },
            value = selectedValue,
            onValueChange = {},
            interactionSource = clickOnlyInteractionSource {
                if (enabled) {
                    expanded = !expanded
                }
            },
            readOnly = true,
            trailingIcon = {
                Icon(
                    Icons.Outlined.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(Spacing.SMALL)
                        .runIf(!enabled) {
                            alpha(0.4f)
                        },
                )
            },
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(with(LocalDensity.current) { dropdownWidth.toDp() }),
        ) {
            val sizeOfOneItem = 50.dp
            val configuration = LocalConfiguration.current
            val screenHeight = remember {
                configuration.screenHeightDp.dp
            }
            val height by remember(items.size) {
                mutableStateOf(minOf(sizeOfOneItem * items.size, screenHeight / 2))
            }

            LazyColumn(
                modifier = Modifier
                    .width(500.dp)
                    .height(height),
            ) {
                items(items) { (key, value) ->
                    DropdownMenuItem(
                        onClick = {
                            expanded = false
                            onItemSelected(key)
                        },
                        text = {
                            itemFactory(key, value)
                        },
                    )
                }
            }
        }
    }
}
