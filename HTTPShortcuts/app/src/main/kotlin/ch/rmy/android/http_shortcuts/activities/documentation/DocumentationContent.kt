package ch.rmy.android.http_shortcuts.activities.documentation

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import ch.rmy.android.http_shortcuts.components.LoadingIndicator
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun DocumentationContent(
    url: Uri,
    onPageChanged: (Uri) -> Unit,
    onPageTitle: (String?) -> Unit,
    onExternalUrl: (Uri) -> Unit,
) {
    var isLoading by remember {
        mutableStateOf(true)
    }
    var isLoadingScreenVisible by remember {
        mutableStateOf(true)
    }
    LaunchedEffect(isLoading) {
        if (isLoading) {
            isLoadingScreenVisible = true
        } else {
            delay(50.milliseconds)
            isLoadingScreenVisible = false
        }
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        if (isLoadingScreenVisible) {
            LoadingIndicator()
        }

        DocumentationBrowser(
            url,
            onPageChanged,
            onPageTitle,
            onLoadingStateChanged = { isLoading = it },
            onExternalUrl,
            modifier = Modifier
                .fillMaxSize()
                .alpha(if (isLoadingScreenVisible) 0f else 1f),
        )
    }
}
