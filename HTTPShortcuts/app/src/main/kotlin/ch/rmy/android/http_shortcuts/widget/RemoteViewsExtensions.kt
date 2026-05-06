package ch.rmy.android.http_shortcuts.widget

import android.widget.ImageView
import android.widget.RemoteViews
import android.widget.TextView
import androidx.annotation.IdRes
import ch.rmy.android.framework.extensions.tryOrLog
import java.lang.reflect.Method

fun RemoteViews.setTextSize(@IdRes id: Int, fontSize: Float) {
    setFloatSafely(TextView::class.java, id, "setTextSize", fontSize)
}

fun RemoteViews.setLineHeight(@IdRes id: Int, lineHeightPx: Int) {
    setTextViewIntSafely(TextView::class.java, id, "setLineHeight", lineHeightPx)
}

fun RemoteViews.setImageViewScaleX(@IdRes id: Int, scaleX: Float) {
    setFloatSafely(ImageView::class.java, id, "setScaleX", scaleX)
}

fun RemoteViews.setImageViewScaleY(@IdRes id: Int, scaleY: Float) {
    setFloatSafely(ImageView::class.java, id, "setScaleY", scaleY)
}

private fun RemoteViews.setFloatSafely(clazz: Class<*>, @IdRes id: Int, methodName: String, value: Float) {
    tryOrLog {
        val method = clazz.getMethod(methodName, Float::class.java)
        if (method.isRemoteViewMethod()) {
            setFloat(id, methodName, value)
        }
    }
}

private fun RemoteViews.setTextViewIntSafely(clazz: Class<*>, @IdRes id: Int, methodName: String, value: Int) {
    tryOrLog {
        val method = clazz::class.java.getMethod(methodName, Int::class.java)
        if (method.isRemoteViewMethod()) {
            setInt(id, methodName, value)
        }
    }
}

private fun Method.isRemoteViewMethod() =
    annotations.any { it.annotationClass.simpleName == "RemotableViewMethod" }
