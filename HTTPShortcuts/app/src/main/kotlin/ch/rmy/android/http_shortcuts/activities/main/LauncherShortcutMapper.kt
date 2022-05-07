package ch.rmy.android.http_shortcuts.activities.main

import ch.rmy.android.http_shortcuts.data.dtos.LauncherShortcut
import ch.rmy.android.http_shortcuts.data.models.CategoryModel
import ch.rmy.android.http_shortcuts.extensions.toLauncherShortcut
import javax.inject.Inject

class LauncherShortcutMapper
@Inject
constructor() {

    operator fun invoke(categories: List<CategoryModel>): List<LauncherShortcut> =
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
