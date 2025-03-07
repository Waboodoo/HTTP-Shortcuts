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
import ch.rmy.android.http_shortcuts.activities.documentation.models.SearchDirection
import ch.rmy.android.http_shortcuts.components.LoadingIndicator
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

@Composable
fun DocumentationContent(
    url: Uri,
    searchQuery: String?,
    searchDirectionRequests: Flow<SearchDirection>,
    onPageChanged: (Uri) -> Unit,
    onPageTitle: (String?) -> Unit,
    onExternalUrl: (Uri) -> Unit,
    onSearchResults: (Int, Int) -> Unit,
) {
    var isLoading by remember {
        mutableStateOf(true)
    }
    var isLoadingScreenVisible by remember {
        mutableStateOf(true)
    }
    LaunchedEffect(isLoading) {
        isLoadingScreenVisible = if (isLoading) {
            true
        } else {
            delay(50.milliseconds)
            false
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
            url = url,
            searchQuery = searchQuery,
            searchDirectionRequests = searchDirectionRequests,
            onPageChanged = onPageChanged,
            onPageTitle = onPageTitle,
            onLoadingStateChanged = { isLoading = it },
            onExternalUrl = onExternalUrl,
            onSearchResults = onSearchResults,
            modifier = Modifier
                .fillMaxSize()
                .alpha(if (isLoadingScreenVisible) 0f else 1f),
        )
    }
}
