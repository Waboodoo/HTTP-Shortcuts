package ch.rmy.android.http_shortcuts.data

import androidx.room.AutoMigration
import androidx.room.Database as DatabaseAnnotation
import androidx.room.RoomDatabase
import ch.rmy.android.http_shortcuts.data.dao.AppLockDao
import ch.rmy.android.http_shortcuts.data.dao.HistoryEventDao
import ch.rmy.android.http_shortcuts.data.dao.WidgetDao
import ch.rmy.android.http_shortcuts.data.models.AppLockModel
import ch.rmy.android.http_shortcuts.data.models.HistoryEventModel
import ch.rmy.android.http_shortcuts.data.models.WidgetModel

@DatabaseAnnotation(
    entities = [
        AppLockModel::class,
        HistoryEventModel::class,
        WidgetModel::class,
    ],
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
    ],
)
abstract class Database : RoomDatabase() {
    abstract fun appLockDao(): AppLockDao
    abstract fun historyEventDao(): HistoryEventDao
    abstract fun widgetDao(): WidgetDao
}
