package ch.rmy.android.http_shortcuts.components

import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun <T : Comparable<T>> OrderedOptionsSlider(
    options: Array<T>,
    value: T,
    enabled: Boolean = true,
    onValueChange: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Slider(
        modifier = modifier,
        valueRange = 0f..(options.lastIndex.toFloat()),
        enabled = enabled,
        value = (
            options.indexOfFirst { it >= value }
                .takeUnless { it == -1 }
                ?: options.lastIndex
            ).toFloat(),
        onValueChange = {
            onValueChange(options[it.toInt()])
        },
    )
}
