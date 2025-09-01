package ch.rmy.android.http_shortcuts.activities.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import ch.rmy.android.http_shortcuts.data.settings.Settings
import ch.rmy.android.http_shortcuts.navigation.NavigationRoot
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseComposeActivity() {

    @Inject
    lateinit var settings: Settings

    @Composable
    override fun Content() {
        Box(
            modifier = Modifier
                .navigationBarsPadding()
                .fillMaxSize(),
        ) {
            NavigationRoot()
        }
    }
}
