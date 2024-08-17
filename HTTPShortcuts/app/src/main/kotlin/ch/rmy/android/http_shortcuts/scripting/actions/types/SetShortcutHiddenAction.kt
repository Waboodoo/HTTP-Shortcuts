package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutNameOrId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import javax.inject.Inject

class SetShortcutHiddenAction
@Inject
constructor(
    private val shortcutRepository: ShortcutRepository,
) : Action<SetShortcutHiddenAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext) {
        setHidden(this.shortcutNameOrId ?: executionContext.shortcutId)
    }

    private suspend fun Params.setHidden(shortcutNameOrId: ShortcutNameOrId) {
        val shortcut = try {
            shortcutRepository.getShortcutByNameOrId(shortcutNameOrId)
        } catch (e: NoSuchElementException) {
            throw ActionException {
                getString(R.string.error_shortcut_not_found_for_set_visible, shortcutNameOrId)
            }
        }
        shortcutRepository.setHidden(shortcut.id, hidden)
    }

    data class Params(
        val shortcutNameOrId: ShortcutNameOrId?,
        val hidden: Boolean,
    )
}
