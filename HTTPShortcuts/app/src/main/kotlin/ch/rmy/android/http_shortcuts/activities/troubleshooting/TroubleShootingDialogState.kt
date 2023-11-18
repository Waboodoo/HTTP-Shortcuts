package ch.rmy.android.http_shortcuts.activities.troubleshooting

import androidx.compose.runtime.Stable

@Stable
sealed class TroubleShootingDialogState {
    object ClearCookies : TroubleShootingDialogState()
}
