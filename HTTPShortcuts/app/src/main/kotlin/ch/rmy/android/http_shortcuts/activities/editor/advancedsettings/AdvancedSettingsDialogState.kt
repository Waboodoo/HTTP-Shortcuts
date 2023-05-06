package ch.rmy.android.http_shortcuts.activities.editor.advancedsettings

import androidx.compose.runtime.Stable
import kotlin.time.Duration

@Stable
sealed class AdvancedSettingsDialogState {
    @Stable
    data class TimeoutPicker(
        val initialTimeout: Duration,
    ) : AdvancedSettingsDialogState()
}
