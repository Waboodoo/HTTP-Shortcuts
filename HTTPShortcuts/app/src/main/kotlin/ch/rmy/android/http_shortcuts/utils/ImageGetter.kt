package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.text.Html.ImageGetter
import android.util.Base64
import androidx.appcompat.graphics.drawable.DrawableWrapper
import androidx.core.graphics.drawable.toDrawable
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.dimen
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.utils.Destroyer
import ch.rmy.android.http_shortcuts.R
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ImageGetter(
    private val context: Context,
    private val onImageLoaded: () -> Unit,
    private val destroyer: Destroyer,
) : ImageGetter {
    override fun getDrawable(source: String?): Drawable {
        val placeholderSize = dimen(context, R.dimen.html_image_placeholder_size)
        val drawableWrapper = DrawableWrapper(context.resources.getDrawable(R.drawable.image_placeholder, null))
        drawableWrapper.setBounds(0, 0, placeholderSize, placeholderSize)

        if (source == null) {
            return drawableWrapper
        }

        Single.fromCallable {
            when {
                source.isBase64EncodedImage() -> {
                    try {
                        val imageAsBytes = source.getBase64ImageData()
                        BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.size)
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
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { drawable ->
                    drawableWrapper.wrappedDrawable = drawable
                    drawableWrapper.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
                    onImageLoaded()
                },
                { error ->
                    if (error !is UnsupportedImageSourceException) {
                        logException(error)
                    }
                    drawableWrapper.wrappedDrawable = context.resources.getDrawable(R.drawable.bitsies_cancel, null)
                    drawableWrapper.setBounds(0, 0, placeholderSize * 2, placeholderSize * 2)
                    onImageLoaded()
                },
            )
            .attachTo(destroyer)

        return drawableWrapper
    }

    private class UnsupportedImageSourceException : Exception()

    companion object {
        private fun String.isBase64EncodedImage(): Boolean =
            matches("^data:image/[^;]+;base64,.+".toRegex(RegexOption.IGNORE_CASE))

        private fun String.getBase64ImageData(): ByteArray =
            dropWhile { it != ',' }
                .drop(1)
                .let {
                    Base64.decode(it, Base64.DEFAULT)
                }
    }
}
