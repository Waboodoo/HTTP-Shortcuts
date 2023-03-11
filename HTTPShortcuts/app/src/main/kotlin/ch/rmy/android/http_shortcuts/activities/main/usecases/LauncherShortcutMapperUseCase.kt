package ch.rmy.android.http_shortcuts.activities.main.usecases

import ch.rmy.android.http_shortcuts.data.dtos.LauncherShortcut
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.extensions.toLauncherShortcut
import javax.inject.Inject

class LauncherShortcutMapperUseCase
@Inject
constructor() {

    operator fun invoke(categories: List<Category>): List<LauncherShortcut> =
        categories.flatMap { category ->
            category.shortcuts
        }
            .filter { shortcut ->
                shortcut.launcherShortcut
            }
            .map { shortcut ->
                shortcut.toLauncherShortcut()
            }
}
