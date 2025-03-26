package ch.rmy.android.http_shortcuts.activities.main.usecases

import ch.rmy.android.http_shortcuts.activities.main.models.RecoveryInfo
import ch.rmy.android.http_shortcuts.data.SessionInfoStore
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import javax.inject.Inject

class ShouldShowRecoveryDialogUseCase
@Inject
constructor(
    private val temporaryShortcutRepository: TemporaryShortcutRepository,
    private val sessionInfoStore: SessionInfoStore,
) {
    suspend operator fun invoke(): RecoveryInfo? {
        val shortcut = try {
            temporaryShortcutRepository.getTemporaryShortcut()
        } catch (_: NoSuchElementException) {
            return null
        }
        val shortcutId = sessionInfoStore.editingShortcutId
        val categoryId = sessionInfoStore.editingShortcutCategoryId
        return if (categoryId != null) {
            // TODO(room): Check whether there are actually unsaved changes
            RecoveryInfo(
                shortcutName = shortcut.name,
                shortcutId = shortcutId,
                categoryId = categoryId,
            )
        } else {
            null
        }
    }
}
