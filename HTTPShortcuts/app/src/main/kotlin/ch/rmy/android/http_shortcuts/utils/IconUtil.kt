package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import ch.rmy.android.http_shortcuts.R

object IconUtil {

    fun getIconURI(context: Context, iconName: String?): Uri = when {
        iconName == null -> Uri.parse("android.resource://${context.packageName}/$DEFAULT_ICON")
        iconName.startsWith("android.resource://") -> Uri.parse(iconName)
        iconName.endsWith(".png") -> Uri.fromFile(context.getFileStreamPath(iconName))
        else -> {
            val identifier = context.resources.getIdentifier(iconName, "drawable", context.packageName)
            Uri.parse("android.resource://${context.packageName}/$identifier")
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    fun getIcon(context: Context, iconName: String?): Icon? = try {
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
                val identifier = context.resources.getIdentifier(iconName, "drawable", context.packageName)
                Icon.createWithResource(context.packageName, identifier)
            }
        }
    } catch (e: Exception) {
        null
    }

    val DEFAULT_ICON = R.drawable.ic_launcher

}