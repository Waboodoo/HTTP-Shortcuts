package ch.rmy.android.http_shortcuts.data

import androidx.room.Database as DatabaseAnnotation
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ch.rmy.android.http_shortcuts.data.dao.AppLockDao
import ch.rmy.android.http_shortcuts.data.dao.HistoryEventDao
import ch.rmy.android.http_shortcuts.data.dao.PendingExecutionDao
import ch.rmy.android.http_shortcuts.data.dao.WidgetDao
import ch.rmy.android.http_shortcuts.data.models.AppLockModel
import ch.rmy.android.http_shortcuts.data.models.HistoryEventModel
import ch.rmy.android.http_shortcuts.data.models.PendingExecutionModel
import ch.rmy.android.http_shortcuts.data.models.ResolvedVariableModel
import ch.rmy.android.http_shortcuts.data.models.WidgetModel

@DatabaseAnnotation(
    entities = [
        AppLockModel::class,
        HistoryEventModel::class,
        PendingExecutionModel::class,
        ResolvedVariableModel::class,
        WidgetModel::class,
    ],
    version = 1,
)
@TypeConverters(Converters::class)
abstract class Database : RoomDatabase() {
    abstract fun appLockDao(): AppLockDao
    abstract fun historyEventDao(): HistoryEventDao
    abstract fun pendingExecutionDao(): PendingExecutionDao
    abstract fun widgetDao(): WidgetDao
}
