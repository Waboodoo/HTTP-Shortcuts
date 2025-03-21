package ch.rmy.android.framework.data

import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.RealmToRoomMigration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

abstract class BaseRepository(
    private val database: Database,
) {
    protected suspend fun <Dao : Any> get(get: Database.() -> Dao): Dao {
        RealmToRoomMigration.migrationDone.await()
        return database.get()
    }

    protected fun <Dao : Any, T> flow(get: Database.() -> Dao, block: Dao.() -> Flow<T>): Flow<T> =
        flow {
            get(get)
                .block()
                .collect(this)
        }
}
