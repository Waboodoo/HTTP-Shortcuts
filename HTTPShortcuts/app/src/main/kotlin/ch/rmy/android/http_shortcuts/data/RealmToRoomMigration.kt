package ch.rmy.android.http_shortcuts.data

import android.content.Context
import androidx.core.content.edit
import ch.rmy.android.framework.data.RealmContext
import ch.rmy.android.framework.data.RealmFactory
import ch.rmy.android.http_shortcuts.data.models.AppLock
import ch.rmy.android.http_shortcuts.data.models.Widget
import ch.rmy.android.http_shortcuts.data.realm.AppLock as AppLockRealmModel
import ch.rmy.android.http_shortcuts.data.realm.Widget as WidgetRealmModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.CompletableDeferred

class RealmToRoomMigration
@Inject
constructor(
    @ApplicationContext
    private val context: Context,
    private val realmFactory: RealmFactory,
    private val database: Database,
) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    suspend fun migrateIfNeeded() {
        val version = preferences.getInt(MIGRATION_VERSION_KEY, 0)
        if (version != MIGRATION_VERSION) {
            val realmContext = realmFactory.getRealmContext()
            if (version < 1) {
                migrateToVersion1(realmContext)
            }
            preferences.edit {
                putInt(MIGRATION_VERSION_KEY, MIGRATION_VERSION)
            }
        }
        migrationDone.complete(Unit)
    }

    private suspend fun migrateToVersion1(realmContext: RealmContext) {
        val widgetDao = database.widgetDao()
        realmContext
            .get<WidgetRealmModel>()
            .find()
            .forEach { widget ->
                val shortcutId = widget.shortcut?.id
                if (shortcutId != null) {
                    widgetDao.insert(
                        Widget(
                            widgetId = widget.widgetId,
                            shortcutId = shortcutId,
                            showLabel = widget.showLabel,
                            showIcon = widget.showIcon,
                            labelColor = widget.labelColor,
                            iconScale = widget.iconScale,
                        ),
                    )
                }
            }

        val appLock = realmContext.get<AppLockRealmModel>()
            .find()
            .firstOrNull()
        if (appLock != null) {
            database.appLockDao().insert(
                AppLock(
                    passwordHash = appLock.passwordHash,
                    useBiometrics = appLock.useBiometrics,
                ),
            )
        }
    }

    companion object {
        private const val PREFERENCES_NAME = "http_shortcuts.realm_to_room_preferences"
        private const val MIGRATION_VERSION_KEY = "migration_version"
        private const val MIGRATION_VERSION = 1

        val migrationDone = CompletableDeferred<Unit>()
    }
}
