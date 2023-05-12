package ch.rmy.android.http_shortcuts.activities.misc.second_launcher

import androidx.compose.runtime.Composable
import ch.rmy.android.framework.ui.Entrypoint
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity

class SecondLauncherActivity : BaseComposeActivity(), Entrypoint {

    override val initializeWithTheme: Boolean
        get() = false

    override val supportsSnackbars: Boolean
        get() = false

    @Composable
    override fun Content() {
        SecondLauncherScreen()
    }
}
