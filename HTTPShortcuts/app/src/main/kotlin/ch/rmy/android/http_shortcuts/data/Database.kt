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
import ch.rmy.android.http_shortcuts.data.domains.sync.SyncDao
import ch.rmy.android.http_shortcuts.data.domains.variables.GlobalVariableDao
import ch.rmy.android.http_shortcuts.data.domains.widgets.ShortcutWidgetDao
import ch.rmy.android.http_shortcuts.data.domains.widgets.VariableWidgetDao
import ch.rmy.android.http_shortcuts.data.domains.working_directories.WorkingDirectoryDao
import ch.rmy.android.http_shortcuts.data.migrations.Migration4
import ch.rmy.android.http_shortcuts.data.models.AppConfig
import ch.rmy.android.http_shortcuts.data.models.AppLock
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.CertificatePin
import ch.rmy.android.http_shortcuts.data.models.GlobalVariable
import ch.rmy.android.http_shortcuts.data.models.HistoryEvent
import ch.rmy.android.http_shortcuts.data.models.PendingExecutionModel
import ch.rmy.android.http_shortcuts.data.models.RequestHeader
import ch.rmy.android.http_shortcuts.data.models.RequestParameter
import ch.rmy.android.http_shortcuts.data.models.ResolvedVariableModel
import ch.rmy.android.http_shortcuts.data.models.Section
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.ShortcutWidget
import ch.rmy.android.http_shortcuts.data.models.SyncConfig
import ch.rmy.android.http_shortcuts.data.models.VariableWidget
import ch.rmy.android.http_shortcuts.data.models.WorkingDirectory

@DatabaseAnnotation(
    entities = [
        AppConfig::class,
        AppLock::class,
        Category::class,
        CertificatePin::class,
        GlobalVariable::class,
        HistoryEvent::class,
        PendingExecutionModel::class,
        RequestHeader::class,
        RequestParameter::class,
        ResolvedVariableModel::class,
        Section::class,
        Shortcut::class,
        ShortcutWidget::class,
        SyncConfig::class,
        VariableWidget::class,
        WorkingDirectory::class,
    ],
    version = 13,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4, spec = Migration4::class),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6),
        AutoMigration(from = 6, to = 7),
        AutoMigration(from = 7, to = 8),
        AutoMigration(from = 8, to = 9),
        AutoMigration(from = 9, to = 10),
        AutoMigration(from = 10, to = 11),
        AutoMigration(from = 11, to = 12),
        AutoMigration(from = 12, to = 13),
    ],
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class Database : RoomDatabase() {
    abstract fun appConfigDao(): AppConfigDao
    abstract fun appLockDao(): AppLockDao
    abstract fun categoryDao(): CategoryDao
    abstract fun certificatePinDao(): CertificatePinDao
    abstract fun globalVariableDao(): GlobalVariableDao
    abstract fun historyEventDao(): HistoryEventDao
    abstract fun pendingExecutionDao(): PendingExecutionDao
    abstract fun requestHeaderDao(): RequestHeaderDao
    abstract fun requestParameterDao(): RequestParameterDao
    abstract fun sectionDao(): SectionDao
    abstract fun shortcutDao(): ShortcutDao
    abstract fun shortcutWidgetDao(): ShortcutWidgetDao
    abstract fun syncDao(): SyncDao
    abstract fun variableWidgetDao(): VariableWidgetDao
    abstract fun workingDirectoryDao(): WorkingDirectoryDao
}
