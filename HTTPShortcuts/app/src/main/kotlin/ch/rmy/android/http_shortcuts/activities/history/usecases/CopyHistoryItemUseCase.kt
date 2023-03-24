package ch.rmy.android.http_shortcuts.activities.history.usecases

import android.content.Context
import ch.rmy.android.framework.utils.ClipboardUtil
import ch.rmy.android.http_shortcuts.activities.history.HistoryListItem
import javax.inject.Inject

class CopyHistoryItemUseCase
@Inject
constructor(
    private val context: Context,
    private val clipboardUtil: ClipboardUtil,
) {
    operator fun invoke(item: HistoryListItem) {
        listOfNotNull(item.title, item.detail)
            .joinToString("\n") {
                it.localize(context)
            }
            .let(clipboardUtil::copyToClipboard)
    }
}
