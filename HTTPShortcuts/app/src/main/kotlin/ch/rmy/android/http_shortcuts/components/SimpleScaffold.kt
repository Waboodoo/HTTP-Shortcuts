package ch.rmy.android.http_shortcuts.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import ch.rmy.android.framework.extensions.runIf

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
    scrollable: Boolean = true,
    actions: @Composable RowScope.(viewState: T) -> Unit = {},
    content: @Composable ColumnScope.(viewState: T) -> Unit,
) {
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
    ) { contentPadding ->
        if (viewState != null) {
            Column(
                modifier = Modifier
                    .padding(contentPadding)
                    .imePadding()
                    .runIf(scrollable) {
                        verticalScroll(rememberScrollState())
                    },
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
