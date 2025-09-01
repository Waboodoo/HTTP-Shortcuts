package ch.rmy.android.http_shortcuts.data.domains.request_parameters

import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.domains.BaseRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.models.RequestParameter
import javax.inject.Inject

class RequestParameterRepository
@Inject
constructor(
    database: Database,
) : BaseRepository(database) {

    suspend fun getRequestParametersByShortcutIds(shortcutIds: List<ShortcutId>): Map<ShortcutId, List<RequestParameter>> = query {
        if (shortcutIds.isEmpty()) {
            emptyMap()
        } else {
            requestParameterDao().getRequestParametersByShortcutIds(shortcutIds).groupBy { it.shortcutId }
        }
    }
}
