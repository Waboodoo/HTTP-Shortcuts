package ch.rmy.android.http_shortcuts.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import ch.rmy.android.http_shortcuts.components.AppTheme
import ch.rmy.android.http_shortcuts.components.Eventinator
import ch.rmy.android.http_shortcuts.components.LocalEventinator

abstract class BaseComposeActivity : BaseActivity() {

    override fun onCreated(savedState: Bundle?) {
        setContent {
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
