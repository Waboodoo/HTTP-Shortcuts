package ch.rmy.android.http_shortcuts.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import ch.rmy.android.framework.extensions.getActivity

enum class BackButton {
    ARROW,
    CROSS,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Any> ScreenScope.SimpleScaffold(
    viewState: T?,
    title: String,
    backButton: BackButton = BackButton.ARROW,
    onBackPressed: (() -> Unit)? = null,
    actions: @Composable RowScope.(viewState: T) -> Unit = {},
    content: @Composable (viewState: T) -> Unit,
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors,
                title = {
                    Text(title)
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onBackPressed?.invoke()
                                ?: run {
                                    context.getActivity()?.finish()
                                }
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
    ) { contentPadding ->
        if (viewState != null) {
            Box(
                modifier = Modifier
                    .padding(contentPadding),
            ) {
                content(viewState)
            }
        } else {
            LoadingIndicatorV(
                modifier = Modifier.padding(contentPadding),
            )
        }
    }
}
