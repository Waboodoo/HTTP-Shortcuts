package ch.rmy.android.http_shortcuts.utils

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Icon
import androidx.annotation.ColorInt
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.getSystemService
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.get
import androidx.core.graphics.scale
import ch.rmy.android.framework.extensions.isNewerThan
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.http_shortcuts.icons.CustomIconName
import ch.rmy.android.http_shortcuts.icons.CustomIconName.Companion.CUSTOM_ICON_NAME_PREFIX
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import java.io.File
import java.io.InputStream
import java.util.regex.Pattern
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object IconUtil {

    private const val ICON_SCALING_FACTOR = 4
    private const val CUSTOM_ICON_MAX_FILE_SIZE = 8 * 1024 * 1024
    private const val CUSTOM_ICON_NAME_REGEX = "custom-icon_([A-Za-z0-9_-]{1,50})\\.(png|jpg)"
    private val CUSTOM_ICON_NAME_PATTERN = CUSTOM_ICON_NAME_REGEX.toPattern(Pattern.CASE_INSENSITIVE)

    fun getIcon(context: Context, icon: ShortcutIcon, adaptive: Boolean = false, backgroundColor: Int? = null): Icon? = try {
        when (icon) {
            is ShortcutIcon.NoIcon -> {
                Icon.createWithResource(context.packageName, ShortcutIcon.NoIcon.iconResource)
            }
            is ShortcutIcon.ExternalResourceIcon -> {
                Icon.createWithResource(icon.packageName, icon.resourceId)
            }
            is ShortcutIcon.CustomIcon -> {
                val file = icon.getFile(context)
                if (file == null) {
                    Icon.createWithResource(context.packageName, ShortcutIcon.NoIcon.iconResource)
                } else {
                    val density = context.resources.displayMetrics.density
                    val options = BitmapFactory.Options()
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888
                    val originalBitmap = BitmapFactory.decodeFile(file.absolutePath, options)
                    val iconColor = icon.tint ?: icon.singleColor

                    if (adaptive) {
                        val outerSize = (108 * density).toInt()

                        // Icons with transparency are well suited to be used in adaptive icons, so we can go with 66dp as recommended by the
                        // guidelines minus some extra padding. Icons without transparency will look weird, so we scale them up a little
                        // so the cropping is less obvious.
                        val innerSize = ((if (icon.isUsableAsSilhouette) 58 else 76) * density).toInt()
                        val offset = (outerSize - innerSize) / 2f
                        val scaledBitmap = originalBitmap.scale(innerSize, innerSize, false)
                            .runIf(icon.isCircular) {
                                try {
                                    croppedToCircle()
                                } finally {
                                    recycle()
                                }
                            }
                        val paddedBitmap = createBitmap(outerSize, outerSize)
                        try {
                            val canvas = Canvas(paddedBitmap)

                            if (backgroundColor != null) {
                                canvas.drawColor(backgroundColor)
                            } else if (iconColor != null) {
                                if (iconColor.isCloseToWhite()) {
                                    canvas.drawARGB(255, 5, 5, 5)
                                } else {
                                    canvas.drawARGB(255, 250, 250, 250)
                                }
                            } else {
                                canvas.drawARGB(255, 5, 5, 5)
                            }

                            val paint = Paint(Paint.FILTER_BITMAP_FLAG)
                            if (iconColor != null) {
                                paint.setColorFilter(PorterDuffColorFilter(iconColor, PorterDuff.Mode.SRC_IN))
                            }
                            paint.isAntiAlias = true
                            canvas.drawBitmap(scaledBitmap, offset, offset, paint)

                            Icon.createWithAdaptiveBitmap(paddedBitmap)
                        } finally {
                            originalBitmap.recycle()
                            scaledBitmap.recycle()
                        }
                    } else {
                        val size = options.outWidth

                        val inputBitmap = if (icon.isCircular) {
                            try {
                                originalBitmap.croppedToCircle()
                            } finally {
                                originalBitmap.recycle()
                            }
                        } else {
                            originalBitmap
                        }
                        try {
                            val canvasBitmap = createBitmap(size, size)
                            val canvas = Canvas(canvasBitmap)

                            if (backgroundColor != null) {
                                canvas.drawColor(backgroundColor)
                            } else {
                                canvas.drawARGB(0, 0, 0, 0)
                            }

                            val paint = Paint(Paint.FILTER_BITMAP_FLAG)
                            if (iconColor != null) {
                                paint.setColorFilter(PorterDuffColorFilter(iconColor, PorterDuff.Mode.SRC_IN))
                            }
                            paint.isAntiAlias = true

                            canvas.drawBitmap(inputBitmap, 0f, 0f, paint)
                            Icon.createWithBitmap(canvasBitmap)
                        } finally {
                            inputBitmap.recycle()
                        }
                    }
                }
            }
            is ShortcutIcon.BuiltInIcon -> {
                if (adaptive) {
                    val file = generateRasterizedIconForBuiltInIcon(context, icon, adaptive = true)
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    Icon.createWithAdaptiveBitmap(bitmap)
                } else {
                    val file = generateRasterizedIconForBuiltInIcon(context, icon)
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    Icon.createWithBitmap(bitmap)
                }
            }
        }
    } catch (_: Exception) {
        null
    }

    private fun Bitmap.croppedToCircle(): Bitmap {
        val outputBitmap = createBitmap(width, height)
        val path = Path()
        path.addCircle(
            (width / 2).toFloat(),
            (height / 2).toFloat(),
            min(width, (height / 2)).toFloat(),
            Path.Direction.CCW,
        )
        val canvas = Canvas(outputBitmap)
        canvas.clipPath(path)
        canvas.drawBitmap(this, 0f, 0f, null)
        return outputBitmap
    }

    fun getRasterizedIconFileName(icon: ShortcutIcon.BuiltInIcon, adaptive: Boolean): String =
        "icon_${if (adaptive) "a_" else ""}${icon.iconName}.png"

    fun isRasterizedIconFileName(fileName: String) =
        fileName.startsWith("icon_") && fileName.endsWith(".png")

    fun generateRasterizedIconForBuiltInIcon(
        context: Context,
        icon: ShortcutIcon.BuiltInIcon,
        adaptive: Boolean = false,
    ): File {
        val fileName = getRasterizedIconFileName(icon, adaptive)
        val file = context.getFileStreamPath(fileName)
        if (file.exists() && file.isNewerThan(3.days)) {
            return file
        }

        val identifier = icon.getDrawableIdentifier(context)
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        val bitmap = if (adaptive) {
            getAdaptiveBitmapFromVectorDrawable(context, identifier, icon.tint, inferBackground = icon.hasBackground)
        } else {
            getBitmapFromVectorDrawable(context, identifier, icon.tint)
        }
        context.openFileOutput(fileName, 0).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            it.flush()
        }
        bitmap.recycle()
        return file
    }

    private fun getBitmapFromVectorDrawable(context: Context, drawableId: Int, tint: Int?): Bitmap {
        val drawable = AppCompatResources.getDrawable(context, drawableId)!!
        val iconSize = getIconSize(context)
        val bitmap = createBitmap(iconSize, iconSize)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, iconSize, iconSize)
        if (tint != null) {
            DrawableCompat.setTint(drawable, tint)
        }
        drawable.draw(canvas)
        return bitmap
    }

    private fun getAdaptiveBitmapFromVectorDrawable(context: Context, drawableId: Int, tint: Int?, inferBackground: Boolean): Bitmap {
        val drawable = AppCompatResources.getDrawable(context, drawableId)!!
        val density = context.resources.displayMetrics.density
        val outerSize = (108 * density).toInt()
        val innerSize = ((if (inferBackground) 64 else 54) * density).toInt()
        val offset = (outerSize - innerSize) / 2
        val bitmap = createBitmap(outerSize, outerSize)
        val canvas = Canvas(bitmap)
        drawable.setBounds(offset, offset, innerSize + offset, innerSize + offset)
        if (tint != null) {
            DrawableCompat.setTint(drawable, tint)
        }
        val backgroundColor = if (inferBackground) {
            drawable.draw(canvas)
            bitmap[offset + 3, outerSize / 2]
                .takeUnless { Color.alpha(it) != 0xFF }
        } else {
            null
        }
            ?: if (tint?.isCloseToWhite() == true) {
                Color.BLACK
            } else {
                Color.WHITE
            }

        canvas.drawColor(backgroundColor)
        drawable.draw(canvas)
        return bitmap
    }

    private fun Int.isCloseToWhite(): Boolean {
        val luminance = Color.valueOf(this).luminance()
        return luminance > 0.7f
    }

    fun createIconFromStream(context: Context, inStream: InputStream): ShortcutIcon? {
        val bitmap = BitmapFactory.decodeStream(inStream)
            ?: return null
        val iconSize = getIconSize(context)
        val scaledBitmap = bitmap.scale(iconSize, iconSize)
        val colorAnalysis = scaledBitmap.analyzeColors()
        val iconName = CustomIconName.generate(
            isCircular = false,
            hasTransparency = colorAnalysis.hasSignificantTransparency,
            singleColor = colorAnalysis.singleColor,
        )
        context.openFileOutput(iconName.toString(), 0).use {
            scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            it.flush()
        }
        scaledBitmap.recycle()
        bitmap.recycle()
        return ShortcutIcon.CustomIcon(iconName)
    }

    fun Bitmap.analyzeColors(): BitmapColorAnalysis {
        val totalPixels = width * height
        val factor = if (totalPixels > 64 * 64) 2 else 1
        var transparentPixels = 0
        var nonTransparentPixels = 0
        val colors = mutableMapOf<Int, Int>()
        for (x in 0 until width step factor) {
            for (y in 0 until height step factor) {
                val color = this[x, y]
                if (color == Color.TRANSPARENT) {
                    transparentPixels++
                } else {
                    val colorWithoutAlpha = color and 0xFFFFFF
                    colors[colorWithoutAlpha] = (colors[colorWithoutAlpha] ?: 0) + 1
                    nonTransparentPixels++
                }
            }
        }

        val singleColor = colors.entries.maxByOrNull { it.value }
            ?.let { mostCommonEntry ->
                mostCommonEntry.takeIf {
                    // The most common color needs to make up at least 95% of the non-transparent pixels
                    it.value > (nonTransparentPixels * 0.95f).toInt()
                }
                    ?.takeIf {
                        // The second most common color needs to make up less than 5% of the non-transparent pixels
                        val secondMostCommonValue = colors.values.filter { it != mostCommonEntry.value }.maxOrNull() ?: 0
                        secondMostCommonValue < (nonTransparentPixels * 0.05f).toInt()
                    }
            }
            ?.key

        // The transparent pixels need to make up at least 10% of all the pixels
        val transparencyThreshold = ((transparentPixels + nonTransparentPixels) * 0.1f).toInt()
        return BitmapColorAnalysis(
            hasSignificantTransparency = transparentPixels >= transparencyThreshold,
            singleColor = singleColor,
        )
    }

    data class BitmapColorAnalysis(
        val hasSignificantTransparency: Boolean,
        @ColorInt
        val singleColor: Int?,
    )

    fun getIconSize(context: Context, scaled: Boolean = true): Int {
        if (iconSizeCached == null) {
            iconSizeCached = max(
                context.resources.getDimensionPixelSize(android.R.dimen.app_icon_size),
                context.getSystemService<ActivityManager>()!!.launcherLargeIconSize,
            )
        }
        return if (scaled) {
            iconSizeCached!! * ICON_SCALING_FACTOR
        } else {
            iconSizeCached!!
        }
    }

    private var iconSizeCached: Int? = null

    fun isCustomIconName(string: String) =
        CustomIconName.parse(string) != null

    fun extractCustomIconNames(string: String): Set<String> =
        buildSet {
            val matcher = CUSTOM_ICON_NAME_PATTERN.matcher(string)
            while (matcher.find()) {
                add(matcher.group())
            }
        }

    suspend fun getCustomIconsInApp(context: Context): List<ShortcutIcon.CustomIcon> =
        withContext(Dispatchers.IO) {
            context.filesDir
                .listFiles { file ->
                    file.name.startsWith(CUSTOM_ICON_NAME_PREFIX) && file.length() < CUSTOM_ICON_MAX_FILE_SIZE
                }
                ?.mapNotNull { CustomIconName.parse(it.name) }
                ?.map { ShortcutIcon.CustomIcon(it) }
                ?: emptyList()
        }
}
