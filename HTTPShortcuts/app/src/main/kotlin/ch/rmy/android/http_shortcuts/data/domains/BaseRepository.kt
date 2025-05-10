package ch.rmy.android.http_shortcuts.data.domains

import androidx.room.withTransaction
import ch.rmy.android.http_shortcuts.data.Database
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow

abstract class BaseRepository(
    private val database: Database,
) {
    protected suspend fun <T> query(get: suspend Database.() -> T): T =
        database.get()

    protected fun <T> queryFlow(get: suspend Database.() -> Flow<T>): Flow<T> =
        flow {
            query(get)
                .distinctUntilChanged()
                .collect(this)
        }

    protected suspend fun <T> commitTransaction(transaction: suspend Database.() -> T): T =
        database.withTransaction {
            transaction(database)
        }
}
