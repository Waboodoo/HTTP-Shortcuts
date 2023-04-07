package ch.rmy.android.http_shortcuts.activities.settings.importexport.usecases

import ch.rmy.android.http_shortcuts.activities.settings.importexport.ImportExportDialogState
import ch.rmy.android.http_shortcuts.components.models.MenuEntry
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import javax.inject.Inject

class GetShortcutSelectionDialogUseCase
@Inject
constructor(
    private val shortcutRepository: ShortcutRepository,
) {
    suspend operator fun invoke(): ImportExportDialogState =
        ImportExportDialogState.ShortcutSelectionForExport(
            shortcutRepository.getShortcuts()
                .map { shortcut ->
                    MenuEntry(
                        key = shortcut.id,
                        name = shortcut.name,
                        icon = shortcut.icon,
                    )
                }
        )
}
