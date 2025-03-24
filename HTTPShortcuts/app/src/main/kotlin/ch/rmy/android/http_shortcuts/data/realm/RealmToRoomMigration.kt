package ch.rmy.android.http_shortcuts.data.realm

import android.content.Context
import androidx.core.content.edit
import androidx.core.net.toUri
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.enums.VariableType
import ch.rmy.android.http_shortcuts.data.models.AppConfig
import ch.rmy.android.http_shortcuts.data.models.AppLock
import ch.rmy.android.http_shortcuts.data.models.Base
import ch.rmy.android.http_shortcuts.data.models.CertificatePin
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.data.models.Widget
import ch.rmy.android.http_shortcuts.data.models.WorkingDirectory
import ch.rmy.android.http_shortcuts.data.realm.models.AppLock as AppLockRealmModel
import ch.rmy.android.http_shortcuts.data.realm.models.CertificatePin as CertificatePinRealmModel
import ch.rmy.android.http_shortcuts.data.realm.models.Widget as WidgetRealmModel
import ch.rmy.android.http_shortcuts.data.realm.models.WorkingDirectory as WorkingDirectoryRealmModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.types.RealmInstant
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.CompletableDeferred
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class RealmToRoomMigration
@Inject
constructor(
    @ApplicationContext
    private val context: Context,
    private val database: Database,
) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    suspend fun migrateIfNeeded(realm: Realm) {
        val version = preferences.getInt(MIGRATION_VERSION_KEY, 0)
        logInfo("Room migration starting at version $version")
        if (version != MIGRATION_VERSION) {
            if (version < 1) {
                migrateToVersion1(realm)
            }
            logInfo("Room migration to version 1 complete")
            if (version < 2) {
                migrateToVersion2(realm)
            }
            logInfo("Room migration to version 2 complete")
            preferences.edit {
                putInt(MIGRATION_VERSION_KEY, MIGRATION_VERSION)
            }
        }
        migrationDone.complete(Unit)
    }

    private suspend fun migrateToVersion1(realm: Realm) {
        val widgetDao = database.widgetDao()
        logInfo("Migrating widgets")

        realm.query<WidgetRealmModel>()
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

        logInfo("Migrating app lock")
        val appLock = realm.query<AppLockRealmModel>()
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

    private suspend fun migrateToVersion2(realm: Realm) {
        logInfo("Migrating certificate pins")
        val certificatePinDao = database.certificatePinDao()
        realm.query<CertificatePinRealmModel>()
            .find()
            .forEach { certificatePin ->
                certificatePinDao.insert(
                    CertificatePin(
                        id = certificatePin.id,
                        pattern = certificatePin.pattern,
                        hash = certificatePin.hash,
                    ),
                )
            }

        logInfo("Migrating working directories")
        val workingDirectoryDao = database.workingDirectoryDao()
        realm.query<WorkingDirectoryRealmModel>()
            .find()
            .forEach { workingDirectory ->
                workingDirectoryDao.insert(
                    WorkingDirectory(
                        id = workingDirectory.id,
                        name = workingDirectory.name,
                        directory = workingDirectory.directory.toUri(),
                        accessed = workingDirectory.accessed?.toInstant(),
                    ),
                )
            }

        logInfo("Migrating app config")
        val appConfigDao = database.appConfigDao()
        realm.query<Base>()
            .find()
            .firstOrNull()
            ?.let { base ->
                appConfigDao.insert(
                    AppConfig(
                        title = base.title.orEmpty(),
                        globalCode = base.globalCode.orEmpty(),
                    ),
                )
            }

        logInfo("Migrating variables")
        val variableDao = database.variableDao()
        realm.query<Base>()
            .find()
            .firstOrNull()
            ?.variables
            ?.mapIndexed { index, variable ->
                Variable(
                    id = variable.id,
                    key = variable.key,
                    type = VariableType.parse(variable.type),
                    value = variable.value,
                    data = run {
                        val data = variable.data?.let { json ->
                            try {
                                JSONObject(json).getJSONObject(variable.type)
                            } catch (e: JSONException) {
                                logException(e)
                                null
                            }
                        }
                            ?: JSONObject()
                        if (variable.options != null && (variable.type == "select" || variable.type == "toggle")) {
                            if (variable.type == "select") {
                                data.put("labels", JSONArray(variable.options!!.map { it.label }))
                            }
                            data.put("values", JSONArray(variable.options!!.map { it.value }))
                        }
                        data.toString().takeUnless { it == "{}" }
                    },
                    rememberValue = variable.rememberValue,
                    urlEncode = variable.urlEncode,
                    jsonEncode = variable.jsonEncode,
                    title = variable.title,
                    message = variable.message,
                    isShareText = variable.flags and 0x1 != 0,
                    isShareTitle = variable.flags and 0x4 != 0,
                    isMultiline = variable.flags and 0x2 != 0,
                    isExcludeValueFromExport = variable.flags and 0x8 != 0,
                    sortingOrder = index,
                )
            }
            ?.let { variables ->
                variableDao.insertAll(variables)
            }
    }

    private fun RealmInstant.toInstant(): Instant =
        Instant.ofEpochSecond(epochSeconds, nanosecondsOfSecond.toLong())

    companion object {
        private const val PREFERENCES_NAME = "http_shortcuts.realm_to_room_preferences"
        private const val MIGRATION_VERSION_KEY = "migration_version"
        private const val MIGRATION_VERSION = 2

        val migrationDone = CompletableDeferred<Unit>()
    }
}
