package ch.rmy.android.http_shortcuts.activities.editor.executionsettings

import androidx.compose.runtime.Stable
import kotlin.time.Duration

@Stable
sealed class ExecutionSettingsDialogState {
    @Stable
    object AppOverlayPrompt : ExecutionSettingsDialogState()

    @Stable
    data class DelayPicker(
        val initialDelay: Duration,
    ) : ExecutionSettingsDialogState()
}
