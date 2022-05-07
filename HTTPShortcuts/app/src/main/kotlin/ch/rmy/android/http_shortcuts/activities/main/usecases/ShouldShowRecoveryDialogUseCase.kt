package ch.rmy.android.http_shortcuts.activities.main.usecases

import ch.rmy.android.framework.utils.Optional
import ch.rmy.android.http_shortcuts.activities.main.models.RecoveryInfo
import ch.rmy.android.http_shortcuts.data.SessionInfoStore
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import io.reactivex.Single
import javax.inject.Inject

class ShouldShowRecoveryDialogUseCase
@Inject
constructor(
    private val temporaryShortcutRepository: TemporaryShortcutRepository,
    private val sessionInfoStore: SessionInfoStore,
) {
    operator fun invoke(): Single<Optional<RecoveryInfo>> =
        temporaryShortcutRepository.getTemporaryShortcut()
            .map { shortcut ->
                val shortcutId = sessionInfoStore.editingShortcutId
                val categoryId = sessionInfoStore.editingShortcutCategoryId
                if (shortcutId != null || categoryId != null) {
                    Optional(
                        RecoveryInfo(
                            shortcutName = shortcut.name,
                            shortcutId = shortcutId,
                            categoryId = categoryId,
                        )
                    )
                } else {
                    Optional.empty()
                }
            }
            .onErrorReturn { Optional.empty() }
}
