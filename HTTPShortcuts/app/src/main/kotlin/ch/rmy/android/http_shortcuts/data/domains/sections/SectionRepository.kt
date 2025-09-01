package ch.rmy.android.http_shortcuts.data.domains.sections

import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.domains.BaseRepository
import ch.rmy.android.http_shortcuts.data.models.Section
import javax.inject.Inject

class SectionRepository
@Inject
constructor(
    database: Database,
) : BaseRepository(database) {
    suspend fun getSections(): List<Section> = query {
        sectionDao().getSections()
    }
}
