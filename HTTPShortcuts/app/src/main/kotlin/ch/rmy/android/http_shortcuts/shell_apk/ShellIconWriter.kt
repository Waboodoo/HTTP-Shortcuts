package ch.rmy.android.http_shortcuts.shell_apk

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.annotation.WorkerThread
import androidx.core.graphics.createBitmap
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.utils.IconUtil
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class ShellIconWriter
@Inject
constructor(
    private val context: Context,
) {

    @WorkerThread
    fun createIconPng(icon: ShortcutIcon): ByteArray {
        val bitmap = loadBitmap(icon) ?: throw InvalidShellApkException()
        return try {
            ByteArrayOutputStream().use { output ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
                output.toByteArray()
            }
        } finally {
            bitmap.recycle()
        }
    }

    private fun loadBitmap(icon: ShortcutIcon): Bitmap? =
        try {
            // Reuse the existing shortcut icon pipeline so custom icons, built-in icons, and external resource icons
            // are rasterized the same way as home-screen shortcuts.
            IconUtil.getIcon(context, icon, adaptive = false)
                ?.loadDrawable(context)
                ?.toBitmap()
        } catch (_: Exception) {
            null
        }

    private fun Drawable.toBitmap(): Bitmap {
        val bitmap = createBitmap(ICON_SIZE, ICON_SIZE)
        val canvas = Canvas(bitmap)
        val oldBounds = copyBounds()
        try {
            setBounds(0, 0, ICON_SIZE, ICON_SIZE)
            draw(canvas)
        } finally {
            bounds = oldBounds
        }
        return bitmap
    }

    companion object {
        private const val ICON_SIZE = 192
    }
}
