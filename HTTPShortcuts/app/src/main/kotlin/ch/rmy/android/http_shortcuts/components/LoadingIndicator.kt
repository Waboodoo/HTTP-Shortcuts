package ch.rmy.android.http_shortcuts.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier),
    ) {
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center),
        )
    }
}
