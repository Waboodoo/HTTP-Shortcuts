package ch.rmy.android.http_shortcuts.activities.history

import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.http_shortcuts.R
import java.util.Date

sealed interface HistoryListItem {

    data class HistoryEvent(
        val id: String,
        val time: Date,
        val title: Localizable,
        val detail: Localizable?,
        val displayType: DisplayType?,
    ) : HistoryListItem {
        enum class DisplayType {
            SUCCESS,
            FAILURE,
        }
    }

    object EmptyState : HistoryListItem {
        val title: Localizable
            get() = StringResLocalizable(R.string.empty_state_history)

        val instructions: Localizable
            get() = Localizable.create {
                it.getString(R.string.empty_state_history_instructions, it.getString(R.string.label_execution_settings))
            }
    }
}
