package ch.rmy.android.http_shortcuts.activities.main.usecases

import ch.rmy.android.http_shortcuts.activities.main.models.RecoveryInfo
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.settings.SessionInfoStore
import javax.inject.Inject

class ShouldShowRecoveryDialogUseCase
@Inject
constructor(
    private val shortcutRepository: ShortcutRepository,
    private val temporaryShortcutRepository: TemporaryShortcutRepository,
    private val sessionInfoStore: SessionInfoStore,
) {
    suspend operator fun invoke(): RecoveryInfo? {
        val temporaryShortcut = try {
            temporaryShortcutRepository.getTemporaryShortcut()
        } catch (_: NoSuchElementException) {
            return null
        }
        val categoryId = sessionInfoStore.editingShortcutCategoryId
            ?: return null

        val shortcutId = sessionInfoStore.editingShortcutId

        val compareTo = if (shortcutId != null) {
            try {
                shortcutRepository.getShortcutById(shortcutId)
            } catch (_: NoSuchElementException) {
                return null
            }
        } else {
            Shortcut.createNew(
                initialIcon = temporaryShortcut.icon,
                executionType = temporaryShortcut.executionType,
                categoryId = categoryId,
            )
        }
            .copy(
                id = temporaryShortcut.id,
                sortingOrder = temporaryShortcut.sortingOrder,
                categoryId = categoryId,
            )

        // TODO: Also compare potential changes in headers and parameters
        if (temporaryShortcut == compareTo) {
            temporaryShortcutRepository.deleteTemporaryShortcut()
            return null
        }

        return RecoveryInfo(
            shortcutName = temporaryShortcut.name,
            shortcutId = shortcutId,
            categoryId = categoryId,
        )
    }
}
