package ch.rmy.android.http_shortcuts.activities.main.usecases

import ch.rmy.android.http_shortcuts.data.models.Category
import javax.inject.Inject

class SecondaryLauncherMapperUseCase
@Inject
constructor() {

    operator fun invoke(categories: List<Category>): Boolean =
        categories.flatMap { category ->
            category.shortcuts
        }
            .any { shortcut ->
                shortcut.secondaryLauncherShortcut
            }
}
