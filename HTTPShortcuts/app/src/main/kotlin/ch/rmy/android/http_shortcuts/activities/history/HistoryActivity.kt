package ch.rmy.android.http_shortcuts.activities.history

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.graphics.Color
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.components.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
class HistoryActivity : BaseActivity() {

    override fun onCreated(savedState: Bundle?) {
        updateStatusBarColor()
        val primaryColor = themeHelper.getPrimaryColor(this)

        setContent {
            val topAppBarColors = TopAppBarDefaults.smallTopAppBarColors(
                containerColor = Color(primaryColor),
                navigationIconContentColor = Color.White,
                titleContentColor = Color.White,
                actionIconContentColor = Color.White,
            )

            AppTheme {
                HistoryScreen(
                    topAppBarColors = topAppBarColors,
                    onEvent = ::handleEvent,
                )
            }
        }
    }

    class IntentBuilder : BaseIntentBuilder(HistoryActivity::class)
}
