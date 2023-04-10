package ch.rmy.android.http_shortcuts.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import kotlinx.coroutines.launch

enum class BackButton {
    ARROW,
    CROSS,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Any> ScreenScope.SimpleScaffold(
    viewState: T?,
    title: String,
    subtitle: String? = null,
    backButton: BackButton = BackButton.ARROW,
    floatingActionButton: @Composable () -> Unit = {},
    actions: @Composable RowScope.(viewState: T) -> Unit = {},
    content: @Composable BoxScope.(viewState: T) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    EventHandler { event ->
        when (event) {
            is ViewModelEvent.ShowSnackbar -> consume {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = event.message.localize(context).toString(),
                        duration = if (event.long) SnackbarDuration.Long else SnackbarDuration.Short,
                    )
                }
            }
            else -> false
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                colors = topAppBarColors,
                title = {
                    Column(
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(title)
                        if (subtitle != null) {
                            Text(
                                subtitle,
                                fontSize = FontSize.SMALL,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onBackPressed()
                        },
                    ) {
                        when (backButton) {
                            BackButton.ARROW -> Icon(Icons.Filled.ArrowBack, null)
                            BackButton.CROSS -> Icon(Icons.Filled.Close, null)
                        }
                    }
                },
                actions = {
                    if (viewState != null) {
                        actions(viewState)
                    }
                },
            )
        },
        floatingActionButton = floatingActionButton,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { contentPadding ->
        if (viewState != null) {
            Box(
                modifier = Modifier
                    .padding(contentPadding)
                    .imePadding()
                    .fillMaxSize(),
            ) {
                content(viewState)
            }
        } else {
            LoadingIndicator(
                modifier = Modifier.padding(contentPadding),
            )
        }
    }
}
