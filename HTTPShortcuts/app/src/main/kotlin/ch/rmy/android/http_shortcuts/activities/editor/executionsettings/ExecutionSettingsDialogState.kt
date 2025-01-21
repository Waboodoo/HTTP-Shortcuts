package ch.rmy.android.http_shortcuts.activities.editor.executionsettings

import androidx.compose.runtime.Stable
import kotlin.time.Duration

@Stable
sealed class ExecutionSettingsDialogState {
    @Stable
    data object AppOverlayPrompt : ExecutionSettingsDialogState()

    @Stable
    data class DelayPicker(
        val initialDelay: Duration,
    ) : ExecutionSettingsDialogState()

    @Stable
    data object RunInBackgroundInfo : ExecutionSettingsDialogState()
}
