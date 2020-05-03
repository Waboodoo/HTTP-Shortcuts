package ch.rmy.android.http_shortcuts.utils

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.extensions.mapIf
import java.io.File
import kotlin.math.max


object IconUtil {

    private const val ICON_SCALING_FACTOR = 2
    val DEFAULT_ICON = R.drawable.ic_launcher

    fun getIconURI(context: Context, iconName: String?, external: Boolean = false): Uri = when {
        iconName == null -> getDrawableUri(context, DEFAULT_ICON)
        iconName.startsWith("android.resource://") -> Uri.parse(iconName)
        iconName.endsWith(".png") -> Uri.fromFile(context.getFileStreamPath(iconName))
        else -> {
            if (external) {
                Uri.fromFile(createIconFromResource(context, iconName))
            } else {
                val identifier = getDrawableIdentifier(context, iconName)
                getDrawableUri(context, identifier)
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    fun getIcon(context: Context, iconName: String?, external: Boolean = false): Icon? = try {
        when {
            iconName == null -> Icon.createWithResource(context.packageName, DEFAULT_ICON)
            iconName.startsWith("android.resource://") -> {
                val pathSegments = Uri.parse(iconName).pathSegments
                Icon.createWithResource(pathSegments[0], Integer.parseInt(pathSegments[1]))
            }
            iconName.endsWith(".png") -> {
                val options = BitmapFactory.Options()
                options.inPreferredConfig = Bitmap.Config.ARGB_8888
                val bitmap = BitmapFactory.decodeFile(context.getFileStreamPath(iconName).absolutePath, options)
                Icon.createWithBitmap(bitmap)
            }
            else -> {
                if (external) {
                    val file = createIconFromResource(context, iconName)
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    Icon.createWithBitmap(bitmap)
                } else {
                    val identifier = getDrawableIdentifier(context, iconName)
                    Icon.createWithResource(context.packageName, identifier)
                }
            }
        }
    } catch (e: Exception) {
        null
    }

    private fun getDrawableUri(context: Context, @DrawableRes identifier: Int): Uri =
        Uri.parse("android.resource://${context.packageName}/$identifier")

    @DrawableRes
    private fun getDrawableIdentifier(context: Context, iconName: String): Int =
        context.resources.getIdentifier(iconName, "drawable", context.packageName)
            .takeUnless { it == 0 }
            ?: DEFAULT_ICON

    private fun createIconFromResource(context: Context, iconName: String): File {
        val fileName = "icon_${iconName}.png"
        val file = context.getFileStreamPath(fileName)
        if (file.exists()) {
            return file
        }

        val identifier = getDrawableIdentifier(context, iconName)
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        val bitmap = getBitmapFromVectorDrawable(context, identifier)
        context.openFileOutput(fileName, 0).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            it.flush()
        }
        bitmap.recycle()
        return file
    }

    private fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context, drawableId)!!
            .mapIf(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                DrawableCompat.wrap(it).mutate()
            }
        val iconSize = getIconSize(context)
        val bitmap = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, iconSize, iconSize)
        drawable.draw(canvas)
        return bitmap
    }

    fun getIconSize(context: Context): Int {
        if (iconSizeCached == null) {
            iconSizeCached = max(
                context.resources.getDimensionPixelSize(android.R.dimen.app_icon_size),
                (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).launcherLargeIconSize
            ) * ICON_SCALING_FACTOR
        }
        return iconSizeCached!!
    }

    private var iconSizeCached: Int? = null

}