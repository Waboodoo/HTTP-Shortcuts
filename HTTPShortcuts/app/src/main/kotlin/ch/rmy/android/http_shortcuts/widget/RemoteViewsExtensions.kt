package ch.rmy.android.http_shortcuts.widget

import android.widget.RemoteViews
import android.widget.TextView
import androidx.annotation.IdRes
import ch.rmy.android.framework.extensions.tryOrLog
import java.lang.reflect.Method

fun RemoteViews.setTextSize(@IdRes id: Int, fontSize: Float) {
    setTextViewFloatSafely(id, "setTextSize", fontSize)
}

fun RemoteViews.setLineHeight(@IdRes id: Int, lineHeightPx: Int) {
    setTextViewIntSafely(id, "setLineHeight", lineHeightPx)
}

private fun RemoteViews.setTextViewFloatSafely(@IdRes id: Int, methodName: String, value: Float) {
    tryOrLog {
        val method = TextView::class.java.getMethod(methodName, Float::class.java)
        if (method.isRemoteViewMethod()) {
            setFloat(id, methodName, value)
        }
    }
}

private fun RemoteViews.setTextViewIntSafely(@IdRes id: Int, methodName: String, value: Int) {
    tryOrLog {
        val method = TextView::class.java.getMethod(methodName, Int::class.java)
        if (method.isRemoteViewMethod()) {
            setInt(id, methodName, value)
        }
    }
}

private fun Method.isRemoteViewMethod() =
    annotations.any { it.annotationClass.simpleName == "RemotableViewMethod" }
