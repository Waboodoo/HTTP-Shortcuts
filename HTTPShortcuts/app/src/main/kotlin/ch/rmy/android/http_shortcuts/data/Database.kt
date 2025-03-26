package ch.rmy.android.http_shortcuts.data

import androidx.room.AutoMigration
import androidx.room.Database as DatabaseAnnotation
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ch.rmy.android.http_shortcuts.data.domains.app_config.AppConfigDao
import ch.rmy.android.http_shortcuts.data.domains.app_lock.AppLockDao
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryDao
import ch.rmy.android.http_shortcuts.data.domains.certificate_pins.CertificatePinDao
import ch.rmy.android.http_shortcuts.data.domains.history.HistoryEventDao
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.PendingExecutionDao
import ch.rmy.android.http_shortcuts.data.domains.request_headers.RequestHeaderDao
import ch.rmy.android.http_shortcuts.data.domains.request_parameters.RequestParameterDao
import ch.rmy.android.http_shortcuts.data.domains.sections.SectionDao
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutDao
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableDao
import ch.rmy.android.http_shortcuts.data.domains.widgets.WidgetDao
import ch.rmy.android.http_shortcuts.data.domains.working_directories.WorkingDirectoryDao
import ch.rmy.android.http_shortcuts.data.models.AppConfig
import ch.rmy.android.http_shortcuts.data.models.AppLock
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.CertificatePin
import ch.rmy.android.http_shortcuts.data.models.HistoryEvent
import ch.rmy.android.http_shortcuts.data.models.PendingExecutionModel
import ch.rmy.android.http_shortcuts.data.models.RequestHeader
import ch.rmy.android.http_shortcuts.data.models.RequestParameter
import ch.rmy.android.http_shortcuts.data.models.ResolvedVariableModel
import ch.rmy.android.http_shortcuts.data.models.Section
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.data.models.Widget
import ch.rmy.android.http_shortcuts.data.models.WorkingDirectory
import ch.rmy.android.http_shortcuts.data.realm.RealmToRoomMigrationDao

@DatabaseAnnotation(
    entities = [
        AppConfig::class,
        AppLock::class,
        Category::class,
        CertificatePin::class,
        HistoryEvent::class,
        PendingExecutionModel::class,
        RequestHeader::class,
        RequestParameter::class,
        ResolvedVariableModel::class,
        Section::class,
        Shortcut::class,
        Variable::class,
        Widget::class,
        WorkingDirectory::class,
    ],
    version = 3,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
    ],
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class Database : RoomDatabase() {
    abstract fun appConfigDao(): AppConfigDao
    abstract fun appLockDao(): AppLockDao
    abstract fun categoryDao(): CategoryDao
    abstract fun certificatePinDao(): CertificatePinDao
    abstract fun historyEventDao(): HistoryEventDao
    abstract fun pendingExecutionDao(): PendingExecutionDao
    abstract fun realmToRoomMigrationDao(): RealmToRoomMigrationDao
    abstract fun requestHeaderDao(): RequestHeaderDao
    abstract fun requestParameterDao(): RequestParameterDao
    abstract fun sectionDao(): SectionDao
    abstract fun shortcutDao(): ShortcutDao
    abstract fun variableDao(): VariableDao
    abstract fun widgetDao(): WidgetDao
    abstract fun workingDirectoryDao(): WorkingDirectoryDao
}
