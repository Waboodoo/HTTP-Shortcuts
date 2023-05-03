package ch.rmy.android.http_shortcuts.components

import android.net.Uri
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.viewinterop.NoOpUpdate
import ch.rmy.android.http_shortcuts.extensions.loadImage

@Composable
fun Image(
    uri: Uri,
    modifier: Modifier = Modifier,
    preventMemoryCache: Boolean = false,
) {
    val context = LocalContext.current
    val imageView = remember {
        ImageView(context)
    }
    AndroidView(
        modifier = modifier,
        factory = {
            imageView.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            imageView.loadImage(uri, preventMemoryCache)
            imageView
        },
        update = NoOpUpdate,
    )
}
