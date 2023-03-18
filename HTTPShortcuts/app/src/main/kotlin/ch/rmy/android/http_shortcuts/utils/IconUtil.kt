package ch.rmy.android.http_shortcuts.utils

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.getSystemService
import androidx.core.graphics.scale
import ch.rmy.android.framework.extensions.setTintCompat
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.time.Instant
import java.util.regex.Pattern
import java.util.regex.Pattern.quote
import kotlin.math.max

object IconUtil {

    private const val ICON_SCALING_FACTOR = 2

    private const val CUSTOM_ICON_NAME_PREFIX = "custom-icon_"
    private const val CUSTOM_ICON_NAME_SUFFIX = ".png"
    private const val CUSTOM_ICON_NAME_ALTERNATIVE_SUFFIX = ".jpg"

    private const val CUSTOM_ICON_MAX_FILE_SIZE = 8 * 1024 * 1024

    private val CUSTOM_ICON_NAME_REGEX = "${quote(CUSTOM_ICON_NAME_PREFIX)}([A-Za-z0-9_-]{1,36})" +
        "(${quote(CUSTOM_ICON_NAME_SUFFIX)}|${quote(CUSTOM_ICON_NAME_ALTERNATIVE_SUFFIX)})"
    private val CUSTOM_ICON_NAME_PATTERN = CUSTOM_ICON_NAME_REGEX.toPattern(Pattern.CASE_INSENSITIVE)

    @RequiresApi(api = Build.VERSION_CODES.M)
    fun getIcon(context: Context, icon: ShortcutIcon): Icon? = try {
        when (icon) {
            is ShortcutIcon.NoIcon -> {
                Icon.createWithResource(context.packageName, ShortcutIcon.NoIcon.ICON_RESOURCE)
            }
            is ShortcutIcon.ExternalResourceIcon -> {
                Icon.createWithResource(icon.packageName, icon.resourceId)
            }
            is ShortcutIcon.CustomIcon -> {
                val file = icon.getFile(context)
                if (file == null) {
                    Icon.createWithResource(context.packageName, ShortcutIcon.NoIcon.ICON_RESOURCE)
                } else {
                    val options = BitmapFactory.Options()
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
                    Icon.createWithBitmap(bitmap)
                }
            }
            is ShortcutIcon.BuiltInIcon -> {
                val file = generateRasterizedIconForBuiltInIcon(context, icon)
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                Icon.createWithBitmap(bitmap)
            }
        }
    } catch (e: Exception) {
        null
    }

    fun generateRasterizedIconForBuiltInIcon(
        context: Context,
        icon: ShortcutIcon.BuiltInIcon,
    ): File {
        val fileName = "icon_${icon.iconName}.png"
        val file = context.getFileStreamPath(fileName)
        if (file.exists()) {
            return file
        }

        val identifier = icon.getDrawableIdentifier(context)
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        val bitmap = getBitmapFromVectorDrawable(context, identifier, icon.tint)
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
        val bitmap = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, iconSize, iconSize)
        if (tint != null) {
            drawable.setTintCompat(tint)
        }
        drawable.draw(canvas)
        return bitmap
    }

    fun createIconFromStream(context: Context, inStream: InputStream): ShortcutIcon? {
        val bitmap = BitmapFactory.decodeStream(inStream)
            ?: return null
        val iconSize = getIconSize(context)
        val scaledBitmap = bitmap.scale(iconSize, iconSize)
        val iconName = generateCustomIconName()
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
                context.getSystemService<ActivityManager>()!!.launcherLargeIconSize
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

    fun generateCustomIconName(): String =
        "${CUSTOM_ICON_NAME_PREFIX}x${Instant.now().toEpochMilli()}$CUSTOM_ICON_NAME_SUFFIX"

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
