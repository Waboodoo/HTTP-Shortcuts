package ch.rmy.android.http_shortcuts.activities.history

import android.os.Bundle
import androidx.activity.compose.setContent
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.components.Screen

class HistoryActivity : BaseActivity() {

    override fun onCreated(savedState: Bundle?) {
        updateStatusBarColor()
        val primaryColor = themeHelper.getPrimaryColor(this)
        setContent {
            Screen(primaryColor, ::handleEvent) {
                HistoryScreen()
            }
        }
    }

    class IntentBuilder : BaseIntentBuilder(HistoryActivity::class)
}
