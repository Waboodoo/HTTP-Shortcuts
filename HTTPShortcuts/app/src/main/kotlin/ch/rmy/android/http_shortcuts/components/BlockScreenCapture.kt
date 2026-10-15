package ch.rmy.android.http_shortcuts.components

import android.view.WindowManager
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

@Composable
fun BlockScreenCapture(enabled: Boolean = true) {
    val activity = LocalActivity.current
    if (enabled && activity != null) {
        DisposableEffect(activity) {
            activity.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            onDispose {
                activity.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
    }
}
