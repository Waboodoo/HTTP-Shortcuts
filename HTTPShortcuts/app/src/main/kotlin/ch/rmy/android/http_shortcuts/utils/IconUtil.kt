package ch.rmy.android.http_shortcuts.utils

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Icon
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.getSystemService
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.get
import androidx.core.graphics.scale
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import java.io.File
import java.io.InputStream
import java.time.Instant
import java.util.regex.Pattern
import java.util.regex.Pattern.quote
import kotlin.math.max
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object IconUtil {

    private const val ICON_SCALING_FACTOR = 4

    private const val CUSTOM_ICON_NAME_PREFIX = "custom-icon_"
    private const val CUSTOM_ICON_NAME_SUFFIX = ".png"
    const val CUSTOM_CIRCULAR_ICON_NAME_SUFFIX = "_circle"
    private const val CUSTOM_ICON_NAME_ALTERNATIVE_SUFFIX = ".jpg"

    private const val CUSTOM_ICON_MAX_FILE_SIZE = 8 * 1024 * 1024

    private val CUSTOM_ICON_NAME_REGEX = "${quote(CUSTOM_ICON_NAME_PREFIX)}([A-Za-z0-9_-]{1,36})" +
        "(${quote(CUSTOM_ICON_NAME_SUFFIX)}|${quote(CUSTOM_ICON_NAME_ALTERNATIVE_SUFFIX)})"
    private val CUSTOM_ICON_NAME_PATTERN = CUSTOM_ICON_NAME_REGEX.toPattern(Pattern.CASE_INSENSITIVE)

    fun getIcon(context: Context, icon: ShortcutIcon, adaptive: Boolean = false): Icon? = try {
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
                    val options = BitmapFactory.Options()
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
                    Icon.createWithBitmap(bitmap)
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

    fun generateRasterizedIconForBuiltInIcon(
        context: Context,
        icon: ShortcutIcon.BuiltInIcon,
        adaptive: Boolean = false,
    ): File {
        val fileName = "icon${if (adaptive) "_a" else ""}_${icon.iconName}.png"
        val file = context.getFileStreamPath(fileName)
        if (file.exists()) {
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

    private fun Int.isCloseToWhite() =
        Color.red(this) > 200 && Color.green(this) > 200 && Color.blue(this) > 200

    fun createIconFromStream(context: Context, inStream: InputStream): ShortcutIcon? {
        val bitmap = BitmapFactory.decodeStream(inStream)
            ?: return null
        val iconSize = getIconSize(context)
        val scaledBitmap = bitmap.scale(iconSize, iconSize)
        val iconName = generateCustomIconName(circular = false)
        context.openFileOutput(iconName, 0).use {
            scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            it.flush()
        }
        scaledBitmap.recycle()
        bitmap.recycle()
        return ShortcutIcon.CustomIcon(iconName)
    }

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
        string.matches(CUSTOM_ICON_NAME_REGEX.toRegex())

    fun generateCustomIconName(circular: Boolean): String =
        "${CUSTOM_ICON_NAME_PREFIX}x" +
            "${Instant.now().toEpochMilli()}" +
            (if (circular) CUSTOM_CIRCULAR_ICON_NAME_SUFFIX else "") +
            CUSTOM_ICON_NAME_SUFFIX

    fun extractCustomIconNames(string: String): Set<String> =
        buildSet {
            val matcher = CUSTOM_ICON_NAME_PATTERN.matcher(string)
            while (matcher.find()) {
                add(matcher.group())
            }
        }

    suspend fun getCustomIconNamesInApp(context: Context): List<String> =
        withContext(Dispatchers.IO) {
            context.filesDir
                .listFiles { file ->
                    file.name.matches(CUSTOM_ICON_NAME_REGEX.toRegex()) && file.length() < CUSTOM_ICON_MAX_FILE_SIZE
                }
                ?.map { it.name }
                ?: emptyList()
        }
}
