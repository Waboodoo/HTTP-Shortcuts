package ch.rmy.android.http_shortcuts.activities.history

import ch.rmy.android.framework.utils.localization.Localizable
import java.time.LocalDateTime

data class HistoryListItem(
    val id: String,
    val time: LocalDateTime,
    val epochMillis: Long,
    val title: Localizable,
    val detail: Localizable?,
    val displayType: DisplayType?,
) {
    enum class DisplayType {
        SUCCESS,
        FAILURE,
    }
}
