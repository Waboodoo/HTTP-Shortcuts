package ch.rmy.android.http_shortcuts.activities.main.usecases

import ch.rmy.android.http_shortcuts.data.models.CategoryModel
import javax.inject.Inject

class SecondaryLauncherMapperUseCase
@Inject
constructor() {

    operator fun invoke(categories: List<CategoryModel>): Boolean =
        categories.flatMap { category ->
            category.shortcuts
        }
            .any { shortcut ->
                shortcut.secondaryLauncherShortcut
            }
}
