package ch.rmy.android.framework.utils

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.AttrRes

object ViewUtil {
    fun getAttributeValue(context: Context, attrs: AttributeSet?, @AttrRes attributeId: Int): Int? =
        context.obtainStyledAttributes(attrs, intArrayOf(attributeId)).use { a ->
            a.getInt(0, -1).takeUnless { it == -1 }
        }
}
