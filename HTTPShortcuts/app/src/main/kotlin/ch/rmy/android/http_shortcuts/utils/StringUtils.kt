package ch.rmy.android.http_shortcuts.utils

import android.text.Spannable
import android.text.SpannableStringBuilder
import ch.rmy.android.framework.utils.spans.OrderedListSpan

object StringUtils {

    fun getOrderedList(items: List<CharSequence>): CharSequence =
        SpannableStringBuilder()
            .apply {
                var offset = 0
                items.forEachIndexed { index, item ->
                    append(item)
                    var length = item.length
                    if (index != items.lastIndex) {
                        append("\n\n")
                        length += 2
                    }
                    setSpan(
                        OrderedListSpan(index + 1),
                        offset,
                        offset + length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
                    )
                    offset += length
                }
            }
}
