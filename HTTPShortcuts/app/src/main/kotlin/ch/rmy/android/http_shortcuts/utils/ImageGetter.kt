package ch.rmy.android.http_shortcuts.utils

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.text.Html.ImageGetter
import android.util.Base64
import androidx.appcompat.graphics.drawable.DrawableWrapper
import androidx.core.graphics.drawable.toDrawable
import ch.rmy.android.framework.extensions.attachTo
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
    private val resources: Resources,
    private val onImageLoaded: () -> Unit,
    private val destroyer: Destroyer,
) : ImageGetter {
    override fun getDrawable(source: String): Drawable {
        val drawableWrapper = DrawableWrapper(
            resources.getDrawable(R.drawable.image_placeholder, null),
        )

        Single.fromCallable {
            val bitmap: Bitmap = if (source.isBase64EncodedImage()) {
                val imageAsBytes = source.getBase64ImageData()
                BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.size)
            } else {
                Picasso.get()
                    .load(source)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .get()
            }
            bitmap.toDrawable(resources)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { drawable ->
                    drawableWrapper.wrappedDrawable = drawable
                    onImageLoaded()
                },
                { error ->
                    logException(error)
                    drawableWrapper.wrappedDrawable = resources.getDrawable(R.drawable.bitsies_cancel, null)
                    onImageLoaded()
                },
            )
            .attachTo(destroyer)

        return drawableWrapper
    }

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
