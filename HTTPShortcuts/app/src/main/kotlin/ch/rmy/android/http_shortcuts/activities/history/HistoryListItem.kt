package ch.rmy.android.http_shortcuts.activities.history

import androidx.compose.runtime.Stable
import ch.rmy.android.framework.utils.localization.Localizable
import java.time.LocalDateTime

@Stable
data class HistoryListItem(
    val id: Int,
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
