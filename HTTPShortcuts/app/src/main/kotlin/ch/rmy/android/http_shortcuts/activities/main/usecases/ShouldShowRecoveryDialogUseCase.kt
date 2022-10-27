package ch.rmy.android.http_shortcuts.activities.main.usecases

import ch.rmy.android.http_shortcuts.activities.main.models.RecoveryInfo
import ch.rmy.android.http_shortcuts.data.SessionInfoStore
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
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
        } catch (e: NoSuchElementException) {
            return null
        }
        val shortcutId = sessionInfoStore.editingShortcutId
        val categoryId = sessionInfoStore.editingShortcutCategoryId
        return if ((shortcutId != null || categoryId != null) && shortcut.hasChanges()) {
            RecoveryInfo(
                shortcutName = shortcut.name,
                shortcutId = shortcutId,
                categoryId = categoryId,
            )
        } else null
    }

    private fun ShortcutModel.hasChanges() =
        !isSameAs(
            ShortcutModel(
                icon = icon.takeIf { it is ShortcutIcon.BuiltInIcon } ?: ShortcutIcon.NoIcon,
                executionType = executionType,
            )
        )
}
