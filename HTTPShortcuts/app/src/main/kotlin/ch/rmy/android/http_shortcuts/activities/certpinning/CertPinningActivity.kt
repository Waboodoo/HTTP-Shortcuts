package ch.rmy.android.http_shortcuts.activities.certpinning

import androidx.compose.runtime.Composable
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CertPinningActivity : BaseComposeActivity() {

    @Composable
    override fun Content() {
        CertPinningScreen()
    }

    class IntentBuilder : BaseIntentBuilder(CertPinningActivity::class)
}
