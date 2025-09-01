package ch.rmy.android.http_shortcuts.data.domains.categories

import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.domains.BaseRepository
import ch.rmy.android.http_shortcuts.data.models.Category
import javax.inject.Inject

class CategoryRepository
@Inject
constructor(
    database: Database,
) : BaseRepository(database) {

    suspend fun getCategories(): List<Category> = query {
        categoryDao().getCategories()
    }
}
