package ch.rmy.android.framework.data

import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.RealmToRoomMigration

abstract class BaseRepository(
    private val database: Database,
) {
    protected suspend fun <T : Any> get(get: Database.() -> T): T {
        RealmToRoomMigration.migrationDone.await()
        return database.get()
    }
}
