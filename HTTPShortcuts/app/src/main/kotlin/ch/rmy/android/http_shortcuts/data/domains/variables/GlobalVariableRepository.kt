package ch.rmy.android.http_shortcuts.data.domains.variables

import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.domains.BaseRepository
import ch.rmy.android.http_shortcuts.data.models.GlobalVariable
import javax.inject.Inject

class GlobalVariableRepository
@Inject
constructor(
    database: Database,
) : BaseRepository(database) {

    suspend fun getGlobalVariables(): List<GlobalVariable> = query {
        globalVariableDao().getVariables()
    }
}
