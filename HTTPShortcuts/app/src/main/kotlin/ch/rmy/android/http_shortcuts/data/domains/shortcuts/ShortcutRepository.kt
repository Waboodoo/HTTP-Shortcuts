package ch.rmy.android.http_shortcuts.data.domains.shortcuts

import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.domains.BaseRepository
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import javax.inject.Inject

class ShortcutRepository
@Inject
constructor(
    database: Database,
) : BaseRepository(database) {

    suspend fun getShortcuts(): List<Shortcut> = query {
        val categories = categoryDao().getCategories()
        val shortcuts = shortcutDao().getShortcuts()
        buildList {
            categories.forEach { category ->
                addAll(shortcuts.filter { it.categoryId == category.id })
            }
        }
    }

    private suspend fun commitTransactionForShortcut(shortcutId: ShortcutId, transaction: suspend Database.(Shortcut) -> Unit) {
        commitTransaction {
            transaction(
                shortcutDao().getShortcutById(shortcutId)
                    .firstOrNull()
                    ?: return@commitTransaction,
            )
        }
    }
}
