package ch.rmy.android.http_shortcuts.activities.certpinning

import androidx.compose.runtime.Composable
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import ch.rmy.android.http_shortcuts.components.ScreenScope

class CertPinningActivity : BaseComposeActivity() {

    @Composable
    override fun ScreenScope.Content() {
        CertPinningScreen()
    }

    class IntentBuilder : BaseIntentBuilder(CertPinningActivity::class)
}
