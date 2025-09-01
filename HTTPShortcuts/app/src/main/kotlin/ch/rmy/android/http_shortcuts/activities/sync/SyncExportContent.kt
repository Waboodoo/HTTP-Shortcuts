package ch.rmy.android.http_shortcuts.activities.sync

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ch.rmy.android.http_shortcuts.activities.sync.components.PasswordProtection
import ch.rmy.android.http_shortcuts.components.Spacing

@Composable
fun SyncExportContent(
    viewState: SyncExportViewState,
    onPasswordChanged: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.MEDIUM)
            .verticalScroll(rememberScrollState()),
    ) {
        PasswordProtection(
            modifier = Modifier.fillMaxWidth(),
            password = viewState.password,
            onPasswordChanged = onPasswordChanged,
        )
    }
}
