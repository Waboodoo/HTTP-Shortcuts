package ch.rmy.android.http_shortcuts.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import ch.rmy.android.http_shortcuts.components.AppTheme
import ch.rmy.android.http_shortcuts.components.Eventinator
import ch.rmy.android.http_shortcuts.components.LocalEventinator
import com.google.accompanist.systemuicontroller.rememberSystemUiController

abstract class BaseComposeActivity : BaseActivity() {

    override fun onCreated(savedState: Bundle?) {
        setContent {
            val systemUiController = rememberSystemUiController()
            val useDarkIcons = !isSystemInDarkTheme()

            DisposableEffect(systemUiController, useDarkIcons) {
                systemUiController.setSystemBarsColor(
                    color = Color.Transparent,
                    darkIcons = useDarkIcons,
                )
                onDispose {}
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
}
