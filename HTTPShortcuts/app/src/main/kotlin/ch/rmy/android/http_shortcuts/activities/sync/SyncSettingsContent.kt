package ch.rmy.android.http_shortcuts.activities.sync

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ch.rmy.android.http_shortcuts.components.Spacing

@Composable
fun SyncSettingsContent(
    viewState: SyncSettingsViewState,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.MEDIUM)
            .verticalScroll(rememberScrollState()),
    ) {
    }
}
