package ch.rmy.android.http_shortcuts.extensions

import android.net.Uri
import android.text.Spanned
import android.text.style.ImageSpan
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.getSpans
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.http_shortcuts.R
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso

@Deprecated("Use Compose instead")
fun ImageView.loadImage(uri: Uri, preventMemoryCache: Boolean = false) {
    Picasso.get()
        .load(uri)
        .noFade()
        .placeholder(R.drawable.image_placeholder)
        .networkPolicy(NetworkPolicy.NO_CACHE)
        .runIf(preventMemoryCache) {
            memoryPolicy(MemoryPolicy.NO_CACHE)
        }
        .error(R.drawable.bitsies_cancel)
        .into(this)
}

fun TextView.reloadImageSpans() {
    text = (text as? Spanned)
        ?.apply {
            getSpans<ImageSpan>()
                .map { it.drawable }
                .forEach { drawable ->
                    if (drawable.intrinsicWidth > width && drawable.intrinsicWidth > 0) {
                        val aspectRatio = drawable.intrinsicWidth / drawable.intrinsicHeight.toDouble()
                        val newImageWidth = width
                        val newImageHeight = (newImageWidth / aspectRatio).toInt()
                        drawable.setBounds(0, 0, newImageWidth, newImageHeight)
                    }
                }
        }
        ?: return
}
