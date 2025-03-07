package ch.rmy.android.http_shortcuts.data

import androidx.room.Database as DatabaseAnnotation
import androidx.room.RoomDatabase
import ch.rmy.android.http_shortcuts.data.dao.HistoryEventDao
import ch.rmy.android.http_shortcuts.data.models.HistoryEventModel

@DatabaseAnnotation(
    entities = [
        HistoryEventModel::class,
    ],
    version = 1,
)
abstract class Database : RoomDatabase() {
    abstract fun historyEventDao(): HistoryEventDao
}
