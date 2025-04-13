package ch.rmy.android.http_shortcuts.data.realm

import android.content.Context
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.room.withTransaction
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.extensions.toCharset
import ch.rmy.android.framework.extensions.truncate
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.Constants
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.dtos.TargetBrowser
import ch.rmy.android.http_shortcuts.data.enums.CategoryBackgroundType
import ch.rmy.android.http_shortcuts.data.enums.CategoryLayoutType
import ch.rmy.android.http_shortcuts.data.enums.ClientCertParams
import ch.rmy.android.http_shortcuts.data.enums.ConfirmationType
import ch.rmy.android.http_shortcuts.data.enums.FileUploadType
import ch.rmy.android.http_shortcuts.data.enums.HttpMethod
import ch.rmy.android.http_shortcuts.data.enums.IpVersion
import ch.rmy.android.http_shortcuts.data.enums.ParameterType
import ch.rmy.android.http_shortcuts.data.enums.ProxyType
import ch.rmy.android.http_shortcuts.data.enums.RequestBodyType
import ch.rmy.android.http_shortcuts.data.enums.ResponseContentType
import ch.rmy.android.http_shortcuts.data.enums.ResponseFailureOutput
import ch.rmy.android.http_shortcuts.data.enums.ResponseSuccessOutput
import ch.rmy.android.http_shortcuts.data.enums.ResponseUiType
import ch.rmy.android.http_shortcuts.data.enums.SecurityPolicy
import ch.rmy.android.http_shortcuts.data.enums.ShortcutAuthenticationType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutClickBehavior
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.enums.VariableType
import ch.rmy.android.http_shortcuts.data.models.AppConfig
import ch.rmy.android.http_shortcuts.data.models.AppLock
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.CertificatePin
import ch.rmy.android.http_shortcuts.data.models.RequestHeader
import ch.rmy.android.http_shortcuts.data.models.RequestParameter
import ch.rmy.android.http_shortcuts.data.models.Section
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.data.models.Widget
import ch.rmy.android.http_shortcuts.data.models.WorkingDirectory
import ch.rmy.android.http_shortcuts.data.realm.models.RealmAppLock
import ch.rmy.android.http_shortcuts.data.realm.models.RealmBase
import ch.rmy.android.http_shortcuts.data.realm.models.RealmCertificatePin
import ch.rmy.android.http_shortcuts.data.realm.models.RealmWidget
import ch.rmy.android.http_shortcuts.data.realm.models.RealmWorkingDirectory
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
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

    private val version = preferences.getInt(MIGRATION_VERSION_KEY, 0)

    fun needsMigration(): Boolean {
        val needsMigration = version < MIGRATION_VERSION
        if (!needsMigration) {
            migrationDone.complete(Unit)
        }
        return needsMigration
    }

    suspend fun migrate() {
        logInfo("Room migration starting at version $version")
        if (version != MIGRATION_VERSION) {
            val realm = RealmFactory.createRealm()
            if (realm != null) {
                database.withTransaction {
                    if (version < 1) {
                        migrateToVersion1(realm)
                    }
                    logInfo("Room migration to version 1 complete")
                    if (version < 2) {
                        migrateToVersion2(realm)
                    }
                    logInfo("Room migration to version 2 complete")
                    if (version < 3) {
                        migrateToVersion3(realm)
                    }
                    logInfo("Room migration to version 3 complete")
                }
                realm.close()
            } else {
                createInitialState()
            }
            preferences.edit {
                putInt(MIGRATION_VERSION_KEY, MIGRATION_VERSION)
            }
        }
        migrationDone.complete(Unit)
    }

    private suspend fun migrateToVersion1(realm: Realm) {
        val widgetDao = database.widgetDao()
        logInfo("Migrating widgets")

        realm.query<RealmWidget>()
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
        val appLock = realm.query<RealmAppLock>()
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
        val migrationDao = database.realmToRoomMigrationDao()

        logInfo("Migrating certificate pins")
        val certificatePinDao = database.certificatePinDao()
        realm.query<RealmCertificatePin>()
            .find()
            .forEach { certificatePin ->
                certificatePinDao.insertCertificatePin(
                    CertificatePin(
                        id = certificatePin.id,
                        pattern = certificatePin.pattern,
                        hash = certificatePin.hash,
                    ),
                )
            }

        logInfo("Migrating working directories")
        val workingDirectoryDao = database.workingDirectoryDao()
        realm.query<RealmWorkingDirectory>()
            .find()
            .forEach { workingDirectory ->
                workingDirectoryDao.insertOrUpdateWorkingDirectory(
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
        realm.query<RealmBase>()
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
        realm.query<RealmBase>()
            .find()
            .firstOrNull()
            ?.variables
            ?.mapIndexed { index, variable ->
                Variable(
                    id = variable.id,
                    key = variable.key,
                    type = VariableType.parse(variable.type) ?: VariableType.CONSTANT,
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
            ?.forEach { variable ->
                migrationDao.insertVariable(variable)
            }
    }

    private suspend fun migrateToVersion3(realm: Realm) {
        logInfo("Migrating categories, sections and shortcuts")
        val migrationDao = database.realmToRoomMigrationDao()
        realm.query<RealmBase>()
            .find()
            .firstOrNull()
            ?.categories
            ?.forEachIndexed { categoryIndex, category ->
                migrationDao.insertCategory(
                    Category(
                        id = category.id,
                        name = category.name,
                        icon = ShortcutIcon.fromName(category.iconName),
                        layoutType = if (category.layoutType == "grid") {
                            CategoryLayoutType.DENSE_GRID
                        } else {
                            CategoryLayoutType.parse(category.layoutType) ?: CategoryLayoutType.LINEAR_LIST
                        },
                        background = CategoryBackgroundType.parse(category.background) ?: CategoryBackgroundType.Default,
                        hidden = category.hidden,
                        scale = 1f,
                        shortcutClickBehavior = category.shortcutClickBehavior?.let { ShortcutClickBehavior.parse(it) },
                        sortingOrder = categoryIndex,
                    ),
                )

                category.sections.forEachIndexed { sectionIndex, section ->
                    migrationDao.insertSection(
                        Section(
                            id = section.id,
                            categoryId = category.id,
                            name = section.name,
                            sortingOrder = sectionIndex,
                        ),
                    )
                }

                category.shortcuts.forEachIndexed { shortcutIndex, shortcut ->
                    val type = shortcut.executionType?.let { ShortcutExecutionType.parse(it) } ?: ShortcutExecutionType.HTTP
                    migrationDao.insertShortcut(
                        Shortcut(
                            id = shortcut.id,
                            executionType = type,
                            categoryId = category.id,
                            name = shortcut.name.truncate(Constants.SHORTCUT_NAME_MAX_LENGTH),
                            description = shortcut.description.truncate(Constants.SHORTCUT_DESCRIPTION_MAX_LENGTH),
                            icon = shortcut.iconName?.let { ShortcutIcon.fromName(it) } ?: ShortcutIcon.NoIcon,
                            hidden = shortcut.hidden,
                            method = HttpMethod.parse(shortcut.method) ?: HttpMethod.GET,
                            url = shortcut.url,
                            authenticationType = shortcut.authentication
                                ?.takeIf { type == ShortcutExecutionType.HTTP }
                                ?.let { ShortcutAuthenticationType.parse(it) },
                            authUsername = shortcut.username,
                            authPassword = shortcut.password,
                            authToken = shortcut.authToken,
                            sectionId = shortcut.section,
                            bodyContent = shortcut.bodyContent,
                            timeout = shortcut.timeout,
                            isWaitForNetwork = shortcut.retryPolicy == "wait_for_internet",
                            securityPolicy = when {
                                shortcut.acceptAllCertificates -> SecurityPolicy.AcceptAll
                                shortcut.certificateFingerprint.isNotEmpty() -> SecurityPolicy.FingerprintOnly(shortcut.certificateFingerprint)
                                else -> null
                            },
                            launcherShortcut = shortcut.launcherShortcut,
                            secondaryLauncherShortcut = shortcut.secondaryLauncherShortcut,
                            quickSettingsTileShortcut = shortcut.quickSettingsTileShortcut,
                            delay = shortcut.delay,
                            repetitionInterval = shortcut.repetition?.interval,
                            contentType = shortcut.contentType,
                            fileUploadType = shortcut.fileUploadOptions?.fileUploadType?.let { FileUploadType.parse(it) },
                            fileUploadSourceFile = shortcut.fileUploadOptions?.file,
                            fileUploadUseImageEditor = shortcut.fileUploadOptions?.useImageEditor == true,
                            confirmationType = shortcut.confirmation?.let { ConfirmationType.parse(it) },
                            followRedirects = shortcut.followRedirects,
                            acceptCookies = shortcut.acceptCookies,
                            keepConnectionOpen = shortcut.keepConnectionOpen,
                            wifiSsid = shortcut.wifiSsid.takeUnlessEmpty()?.takeIf { type == ShortcutExecutionType.HTTP },
                            codeOnPrepare = shortcut.codeOnPrepare,
                            codeOnSuccess = shortcut.codeOnSuccess,
                            codeOnFailure = shortcut.codeOnFailure,
                            targetBrowser = TargetBrowser.parse(shortcut.browserPackageName),
                            excludeFromHistory = shortcut.excludeFromHistory,
                            clientCertParams = ClientCertParams.parse(shortcut.clientCert)?.takeIf { type == ShortcutExecutionType.HTTP },
                            requestBodyType = RequestBodyType.parse(shortcut.requestBodyType) ?: RequestBodyType.CUSTOM_TEXT,
                            ipVersion = shortcut.protocolVersion?.let { IpVersion.parse(it) }?.takeIf { type == ShortcutExecutionType.HTTP },
                            proxyType = ProxyType.parse(shortcut.proxy)?.takeIf { type == ShortcutExecutionType.HTTP },
                            proxyHost = shortcut.proxyHost?.takeIf { type == ShortcutExecutionType.HTTP },
                            proxyPort = shortcut.proxyPort?.takeIf { type == ShortcutExecutionType.HTTP },
                            proxyUsername = shortcut.proxyUsername?.takeIf { type == ShortcutExecutionType.HTTP },
                            proxyPassword = shortcut.proxyPassword?.takeIf { type == ShortcutExecutionType.HTTP },
                            excludeFromFileSharing = shortcut.excludeFromFileSharing,
                            runInForegroundService = shortcut.runInForegroundService,
                            wolMacAddress = shortcut.wolMacAddress,
                            wolPort = shortcut.wolPort,
                            wolBroadcastAddress = shortcut.wolBroadcastAddress,
                            responseActions = shortcut.responseHandling?.actions?.joinToString(separator = ",") ?: "",
                            responseUiType = shortcut.responseHandling?.uiType?.let { ResponseUiType.parse(it) } ?: ResponseUiType.WINDOW,
                            responseSuccessOutput = shortcut.responseHandling?.successOutput?.let { ResponseSuccessOutput.parse(it) }
                                ?: ResponseSuccessOutput.RESPONSE,
                            responseFailureOutput = shortcut.responseHandling?.failureOutput?.let { ResponseFailureOutput.parse(it) }
                                ?: ResponseFailureOutput.DETAILED,
                            responseContentType = shortcut.responseHandling?.contentType?.let { ResponseContentType.parse(it) },
                            responseCharset = shortcut.responseHandling?.charset?.toCharset(),
                            responseSuccessMessage = shortcut.responseHandling?.successMessage ?: "",
                            responseIncludeMetaInfo = shortcut.responseHandling?.includeMetaInfo == true,
                            responseJsonArrayAsTable = shortcut.responseHandling?.jsonArrayAsTable == true,
                            responseMonospace = shortcut.responseHandling?.monospace == true,
                            responseFontSize = shortcut.responseHandling?.fontSize,
                            responseJavaScriptEnabled = shortcut.responseHandling?.javaScriptEnabled == true,
                            responseStoreDirectoryId = shortcut.responseHandling?.storeDirectoryId,
                            responseStoreFileName = shortcut.responseHandling?.storeFileName,
                            responseReplaceFileIfExists = shortcut.responseHandling?.replaceFileIfExists == true,
                            sortingOrder = shortcutIndex,
                        ),
                    )

                    shortcut.headers.forEachIndexed { headerIndex, header ->
                        migrationDao.insertRequestHeader(
                            RequestHeader(
                                shortcutId = shortcut.id,
                                key = header.key,
                                value = header.value,
                                sortingOrder = headerIndex,
                            ),
                        )
                    }

                    shortcut.parameters.forEachIndexed { parameterIndex, parameter ->
                        migrationDao.insertRequestParameter(
                            RequestParameter(
                                shortcutId = shortcut.id,
                                key = parameter.key,
                                value = parameter.value,
                                parameterType = ParameterType.parse(parameter.type) ?: ParameterType.STRING,
                                fileUploadType = parameter.fileUploadOptions?.fileUploadType?.let { FileUploadType.parse(it) },
                                fileUploadSourceFile = parameter.fileUploadOptions?.file,
                                fileUploadFileName = parameter.fileName,
                                fileUploadUseImageEditor = parameter.fileUploadOptions?.useImageEditor == true,
                                sortingOrder = parameterIndex,
                            ),
                        )
                    }
                }
            }
    }

    private suspend fun createInitialState() {
        database.realmToRoomMigrationDao()
            .insertCategory(
                Category(
                    id = newUUID(),
                    name = context.getString(R.string.shortcuts),
                    icon = null,
                    layoutType = CategoryLayoutType.LINEAR_LIST,
                    background = CategoryBackgroundType.Default,
                    hidden = false,
                    scale = 1f,
                    shortcutClickBehavior = null,
                    sortingOrder = 0,
                ),
            )
    }

    private fun RealmInstant.toInstant(): Instant =
        Instant.ofEpochSecond(epochSeconds, nanosecondsOfSecond.toLong())

    companion object {
        private const val PREFERENCES_NAME = "http_shortcuts.realm_to_room_preferences"
        private const val MIGRATION_VERSION_KEY = "migration_version"
        private const val MIGRATION_VERSION = 3

        val migrationDone = CompletableDeferred<Unit>()
    }
}
