package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.annotation.AttrRes

object ViewUtil {

    fun getAttributeValue(context: Context, attrs: AttributeSet?, @AttrRes attributeId: Int): Int? {
        var ta: TypedArray? = null
        try {
            ta = context.obtainStyledAttributes(attrs, intArrayOf(attributeId))
            return ta.getInt(0, -1).takeUnless { it == -1 }
        } finally {
            ta?.recycle()
        }
    }

}