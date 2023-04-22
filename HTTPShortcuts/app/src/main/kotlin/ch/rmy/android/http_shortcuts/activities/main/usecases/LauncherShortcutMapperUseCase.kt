package ch.rmy.android.http_shortcuts.activities.main.usecases

import ch.rmy.android.http_shortcuts.data.dtos.ShortcutPlaceholder
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.toShortcutPlaceholder
import javax.inject.Inject

class LauncherShortcutMapperUseCase
@Inject
constructor() {

    operator fun invoke(categories: List<Category>): List<ShortcutPlaceholder> =
        categories.flatMap(Category::shortcuts)
            .filter(Shortcut::launcherShortcut)
            .map(Shortcut::toShortcutPlaceholder)
}
