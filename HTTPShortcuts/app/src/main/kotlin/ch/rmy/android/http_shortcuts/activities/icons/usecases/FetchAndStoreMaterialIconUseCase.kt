package ch.rmy.android.http_shortcuts.activities.icons.usecases

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.annotation.CheckResult
import androidx.annotation.ColorInt
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import ch.rmy.android.http_shortcuts.activities.icons.createImageLoader
import ch.rmy.android.http_shortcuts.activities.icons.models.MaterialIcon
import ch.rmy.android.http_shortcuts.icons.CustomIconName
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.utils.IconUtil
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.toBitmap
import java.io.File
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FetchAndStoreMaterialIconUseCase
@Inject
constructor(
    private val context: Context,
) {
    @CheckResult
    suspend operator fun invoke(icon: MaterialIcon, @ColorInt color: Int): ShortcutIcon.CustomIcon {
        val iconName = CustomIconName.generate(
            prefix = "md-${icon.name}",
            isCircular = false,
            hasTransparency = true,
            singleColor = color,
        )
        val targetFile = File(context.filesDir, iconName.toString())
        try {
            // TODO: The file/bitmap/compression handling should probably not live in a usecase
            withContext(Dispatchers.IO) {
                val bitmap = fetchIconAsBitmap(icon.url).tint(color)
                targetFile.outputStream().use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.flush()
                }
                bitmap.recycle()
            }
        } catch (e: Throwable) {
            targetFile.delete()
            throw e
        }
        return ShortcutIcon.CustomIcon(iconName)
    }

    private suspend fun fetchIconAsBitmap(url: String): Bitmap {
        val size = IconUtil.getIconSize(context)
        val result = createImageLoader(context)
            .execute(
                ImageRequest.Builder(context)
                    .data(url)
                    .size(size)
                    .build(),
            )
        when (result) {
            is SuccessResult -> return result.image.toBitmap(width = size, height = size)
            is ErrorResult -> throw if (result.throwable is IOException) {
                result.throwable
            } else {
                IOException(result.throwable)
            }
        }
    }

    private fun Bitmap.tint(color: Int): Bitmap {
        val sourceBitmap = this
        try {
            val paint = Paint()
            paint.setColorFilter(PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN))
            return createBitmap(width, height).applyCanvas {
                drawBitmap(sourceBitmap, 0f, 0f, paint)
            }
        } finally {
            sourceBitmap.recycle()
        }
    }
}
