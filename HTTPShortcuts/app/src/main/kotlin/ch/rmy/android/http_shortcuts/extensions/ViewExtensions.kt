package ch.rmy.android.http_shortcuts.extensions

import android.net.Uri
import android.widget.ImageView
import ch.rmy.android.framework.extensions.mapIf
import ch.rmy.android.http_shortcuts.R
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso

fun ImageView.loadImage(uri: Uri, preventMemoryCache: Boolean = false) {
    Picasso.get()
        .load(uri)
        .noFade()
        .placeholder(R.drawable.image_placeholder)
        .networkPolicy(NetworkPolicy.NO_CACHE)
        .mapIf(preventMemoryCache) {
            memoryPolicy(MemoryPolicy.NO_CACHE)
        }
        .error(R.drawable.bitsies_cancel)
        .into(this)
}
