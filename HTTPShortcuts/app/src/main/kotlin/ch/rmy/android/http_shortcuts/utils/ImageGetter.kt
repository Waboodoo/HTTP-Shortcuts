package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.text.Html.ImageGetter
import android.util.Base64
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toDrawable
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.http_shortcuts.R
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImageGetter(
    private val context: Context,
    private val onImageLoaded: () -> Unit,
    private val coroutineScope: CoroutineScope,
) : ImageGetter {
    override fun getDrawable(source: String?): Drawable {
        val placeholderSize = context.resources.getDimensionPixelSize(R.dimen.html_image_placeholder_size)
        val drawableWrapper = DrawableWrapper(ResourcesCompat.getDrawable(context.resources, R.drawable.image_placeholder, null)!!)
        drawableWrapper.setBounds(0, 0, placeholderSize, placeholderSize)

        if (source == null) {
            return drawableWrapper
        }

        coroutineScope.launch {
            try {
                val drawable = withContext(Dispatchers.IO) {
                    when {
                        source.isBase64EncodedImage() -> {
                            try {
                                val imageAsBytes = source.getBase64ImageData()
                                BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.size)
                                    ?: throw UnsupportedImageSourceException()
                            } catch (e: IllegalArgumentException) {
                                throw UnsupportedImageSourceException()
                            }
                        }
                        source.run { startsWith("https://", ignoreCase = true) || startsWith("http://", ignoreCase = true) } -> {
                            Picasso.get()
                                .load(source)
                                .networkPolicy(NetworkPolicy.NO_CACHE)
                                .memoryPolicy(MemoryPolicy.NO_CACHE)
                                .get()
                        }
                        else -> throw UnsupportedImageSourceException()
                    }
                        .toDrawable(context.resources)
                }
                drawableWrapper.wrappedDrawable = drawable
                drawableWrapper.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
                onImageLoaded()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (e !is UnsupportedImageSourceException) {
                    logException(e)
                }
                drawableWrapper.wrappedDrawable = ResourcesCompat.getDrawable(context.resources, R.drawable.bitsies_cancel, null)!!
                drawableWrapper.setBounds(0, 0, placeholderSize * 2, placeholderSize * 2)
                onImageLoaded()
            }
        }

        return drawableWrapper
    }

    private class UnsupportedImageSourceException : Exception()

    companion object {
        internal fun String.isBase64EncodedImage(): Boolean =
            matches("^data:image/[^;]+;base64,.+".toRegex(RegexOption.IGNORE_CASE))

        internal fun String.getBase64ImageData(): ByteArray =
            dropWhile { it != ',' }
                .drop(1)
                .let {
                    Base64.decode(it, Base64.DEFAULT)
                }
    }
}
