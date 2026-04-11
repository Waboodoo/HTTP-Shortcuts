package ch.rmy.android.http_shortcuts.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ch.rmy.android.http_shortcuts.R

@Composable
fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.SMALL),
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = query,
            onValueChange = onQueryChanged,
            placeholder = {
                Text(stringResource(R.string.menu_action_search))
            },
            leadingIcon = {
                Icon(painterResource(R.drawable.outline_search_24), contentDescription = null)
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    Icon(
                        painterResource(R.drawable.outline_close_24),
                        contentDescription = stringResource(R.string.accessibility_clear_search),
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = false),
                            onClick = {
                                onQueryChanged("")
                            },
                        ),
                    )
                }
            },
            maxLines = 1,
            singleLine = true,
        )
    }
}

@Preview
@Composable
private fun SearchBar_Empty_Preview() {
    SearchBar(
        query = "",
        onQueryChanged = {},
    )
}

@Preview
@Composable
private fun SearchBar_Filled_Preview() {
    SearchBar(
        query = "foobar",
        onQueryChanged = {},
    )
}
