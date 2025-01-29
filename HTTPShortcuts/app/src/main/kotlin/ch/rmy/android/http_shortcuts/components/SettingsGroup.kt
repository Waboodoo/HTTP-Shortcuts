package ch.rmy.android.http_shortcuts.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics

@Composable
fun SettingsGroup(
    modifier: Modifier = Modifier,
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface {
        Column(
            modifier = modifier.fillMaxWidth(),
        ) {
            SettingsGroupTitle(title)
            content()
        }
    }
}

@Composable
internal fun SettingsGroupTitle(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.MEDIUM)
            .padding(top = Spacing.MEDIUM, bottom = Spacing.TINY)
            .semantics(mergeDescendants = true) {
                heading()
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        ProvideTextStyle(value = MaterialTheme.typography.headlineMedium.copy(color = MaterialTheme.colorScheme.primary)) {
            Text(title)
        }
    }
}
