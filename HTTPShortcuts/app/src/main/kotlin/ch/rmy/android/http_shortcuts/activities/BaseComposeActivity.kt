package ch.rmy.android.http_shortcuts.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import ch.rmy.android.http_shortcuts.components.Screen
import ch.rmy.android.http_shortcuts.components.ScreenScope

abstract class BaseComposeActivity : BaseActivity() {

    override fun onCreated(savedState: Bundle?) {
        updateStatusBarColor()
        val primaryColor = themeHelper.getPrimaryColor(this)
        setContent {
            Screen(primaryColor, ::handleEvent) {
                Content()
            }
        }
    }

    @Composable
    abstract fun ScreenScope.Content()
}
