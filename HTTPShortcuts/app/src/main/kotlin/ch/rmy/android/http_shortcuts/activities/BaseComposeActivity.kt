package ch.rmy.android.http_shortcuts.activities

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import ch.rmy.android.http_shortcuts.components.AppTheme
import ch.rmy.android.http_shortcuts.components.Eventinator
import ch.rmy.android.http_shortcuts.components.LocalEventinator

abstract class BaseComposeActivity : BaseActivity() {

    override fun onCreated(savedState: Bundle?) {
        setContent {
            val darkTheme = isSystemInDarkTheme()
            SideEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    ) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(
                        lightScrim,
                        darkScrim,
                    ) { darkTheme },
                )
            }

            val eventinator = remember {
                Eventinator(::handleEvent)
            }

            AppTheme {
                CompositionLocalProvider(LocalEventinator provides eventinator) {
                    Content()
                }
            }
        }
    }

    @Composable
    abstract fun Content()

    companion object {
        private val lightScrim = android.graphics.Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
        private val darkScrim = android.graphics.Color.argb(0x80, 0x1b, 0x1b, 0x1b)
    }
}
