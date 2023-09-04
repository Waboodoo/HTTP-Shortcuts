package ch.rmy.android.http_shortcuts.activities.misc.second_launcher

import androidx.compose.runtime.Composable
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SecondLauncherActivity : BaseComposeActivity() {

    override val initializeWithTheme: Boolean
        get() = false

    @Composable
    override fun Content() {
        SecondLauncherScreen()
    }
}
