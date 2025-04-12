package ch.rmy.android.http_shortcuts.import_export

import ch.rmy.android.framework.extensions.falseOrNull
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.extensions.trueOrNull
import ch.rmy.android.http_shortcuts.data.domains.app_config.AppConfigRepository
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryRepository
import ch.rmy.android.http_shortcuts.data.domains.certificate_pins.CertificatePinRepository
import ch.rmy.android.http_shortcuts.data.domains.request_headers.RequestHeaderRepository
import ch.rmy.android.http_shortcuts.data.domains.request_parameters.RequestParameterRepository
import ch.rmy.android.http_shortcuts.data.domains.sections.SectionRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.domains.working_directories.WorkingDirectoryRepository
import ch.rmy.android.http_shortcuts.data.enums.CategoryBackgroundType
import ch.rmy.android.http_shortcuts.data.enums.CategoryLayoutType
import ch.rmy.android.http_shortcuts.data.enums.ParameterType
import ch.rmy.android.http_shortcuts.data.enums.RequestBodyType
import ch.rmy.android.http_shortcuts.data.enums.ResponseFailureOutput
import ch.rmy.android.http_shortcuts.data.enums.ResponseSuccessOutput
import ch.rmy.android.http_shortcuts.data.enums.ResponseUiType
import ch.rmy.android.http_shortcuts.data.enums.SecurityPolicy
import ch.rmy.android.http_shortcuts.data.enums.ShortcutAuthenticationType.BASIC
import ch.rmy.android.http_shortcuts.data.enums.ShortcutAuthenticationType.BEARER
import ch.rmy.android.http_shortcuts.data.enums.ShortcutAuthenticationType.DIGEST
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType.HTTP
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType.MQTT
import ch.rmy.android.http_shortcuts.data.enums.VariableType
import ch.rmy.android.http_shortcuts.extensions.getRequestHeadersForShortcuts
import ch.rmy.android.http_shortcuts.extensions.getRequestParametersForShortcuts
import ch.rmy.android.http_shortcuts.extensions.usesUrl
import ch.rmy.android.http_shortcuts.import_export.models.ExportBase
import ch.rmy.android.http_shortcuts.import_export.models.ExportCategory
import ch.rmy.android.http_shortcuts.import_export.models.ExportCertificatePin
import ch.rmy.android.http_shortcuts.import_export.models.ExportFileUploadOptions
import ch.rmy.android.http_shortcuts.import_export.models.ExportHeader
import ch.rmy.android.http_shortcuts.import_export.models.ExportParameter
import ch.rmy.android.http_shortcuts.import_export.models.ExportResponseHandling
import ch.rmy.android.http_shortcuts.import_export.models.ExportSection
import ch.rmy.android.http_shortcuts.import_export.models.ExportShortcut
import ch.rmy.android.http_shortcuts.import_export.models.ExportVariable
import ch.rmy.android.http_shortcuts.import_export.models.ExportWorkingDirectory
import ch.rmy.android.http_shortcuts.usecases.GetUsedWorkingDirectoryIdsUseCase
import javax.inject.Inject
import kotlin.collections.filter
import kotlin.collections.map

class ExportBaseLoader
@Inject
constructor(
    private val appConfigRepository: AppConfigRepository,
    private val shortcutRepository: ShortcutRepository,
    private val requestHeaderRepository: RequestHeaderRepository,
    private val requestParameterRepository: RequestParameterRepository,
    private val categoryRepository: CategoryRepository,
    private val sectionRepository: SectionRepository,
    private val variableRepository: VariableRepository,
    private val certificatePinRepository: CertificatePinRepository,
    private val workingDirectoryRepository: WorkingDirectoryRepository,
    private val getUsedWorkingDirectoryIds: GetUsedWorkingDirectoryIdsUseCase,
) {
    suspend fun getBase(
        shortcutIds: Collection<ShortcutId>?,
        variableIds: Collection<VariableId>?,
        excludeVariableValuesIfNeeded: Boolean,
    ): ExportBase {
        val shortcuts = shortcutRepository.getShortcuts()
            .runIfNotNull(shortcutIds) { shortcutIds ->
                filter { shortcut -> shortcut.id in shortcutIds }
            }
        val shortcutsByCategoryId = shortcuts.groupBy { it.categoryId }
        val categoriesIdsInUse = shortcutsByCategoryId.keys
        val categories = categoryRepository.getCategories()
            .runIfNotNull(shortcutIds) {
                filter { it.id in categoriesIdsInUse }
            }
        val sections = sectionRepository.getSections()
            .runIfNotNull(shortcutIds) {
                filter { section -> shortcuts.any { it.sectionId == section.id } }
            }
        val sectionsByCategoryId = sections.groupBy { it.categoryId }

        val variables = variableRepository.getVariables()
            .runIfNotNull(variableIds) { variableIds ->
                filter { it.id in variableIds }
            }

        val appConfig = appConfigRepository.getAppConfig()
        val relevantWorkingDirectoryIds = getUsedWorkingDirectoryIds(shortcuts, appConfig)

        val requestHeadersByShortcutId = requestHeaderRepository.getRequestHeadersForShortcuts(shortcuts)
        val requestParametersByShortcutId = requestParameterRepository.getRequestParametersForShortcuts(shortcuts)

        return ExportBase(
            version = ImportMigrator.VERSION,
            compatibilityVersion = ImportMigrator.COMPATIBILITY_VERSION,
            categories = categories.map { category ->
                ExportCategory(
                    id = category.id,
                    name = category.name,
                    iconName = category.icon?.toString()?.takeUnlessEmpty(),
                    layoutType = category.layoutType.takeIf { it != CategoryLayoutType.LINEAR_LIST }?.type,
                    background = category.background.takeIf { it != CategoryBackgroundType.Default }?.serialize(),
                    hidden = category.hidden.trueOrNull(),
                    scale = category.scale.takeUnless { it == 1f },
                    shortcutClickBehavior = category.shortcutClickBehavior?.type,
                    sections = sectionsByCategoryId[category.id]
                        ?.map { section ->
                            ExportSection(
                                id = section.id,
                                name = section.name,
                            )
                        },
                    shortcuts = shortcutsByCategoryId[category.id]
                        ?.map { shortcut ->
                            val type = shortcut.executionType
                            val authType = shortcut.authenticationType
                            ExportShortcut(
                                id = shortcut.id,
                                executionType = shortcut.executionType
                                    .takeIf { it != HTTP }
                                    ?.type,
                                name = shortcut.name,
                                description = shortcut.description.takeUnlessEmpty(),
                                iconName = shortcut.icon.toString(),
                                hidden = shortcut.hidden.trueOrNull(),
                                method = shortcut.takeIf { type == HTTP }?.method?.method,
                                url = shortcut.takeIf { shortcut.executionType.usesUrl }?.url,
                                username = shortcut
                                    .takeIf { (type == HTTP && (authType == BASIC || authType == DIGEST)) || type == MQTT }
                                    ?.authUsername
                                    ?.takeUnlessEmpty(),
                                password = shortcut
                                    .takeIf { (type == HTTP && (authType == BASIC || authType == DIGEST)) || type == MQTT }
                                    ?.authPassword
                                    ?.takeUnlessEmpty(),
                                authToken = shortcut
                                    .takeIf { (type == HTTP && authType == BEARER) || type == MQTT }
                                    ?.authToken
                                    ?.takeUnlessEmpty(),
                                section = shortcut.sectionId,
                                bodyContent = shortcut.takeIf { type == HTTP }?.bodyContent,
                                timeout = shortcut.takeIf { type == HTTP }?.timeout?.takeIf { it != 10_000 },
                                waitForInternet = shortcut.takeIf { type == HTTP }?.isWaitForNetwork?.trueOrNull(),
                                acceptAllCertificates = (shortcut.takeIf { type == HTTP }?.securityPolicy == SecurityPolicy.AcceptAll).trueOrNull(),
                                certificateFingerprint = (shortcut.securityPolicy as? SecurityPolicy.FingerprintOnly)?.certificateFingerprint,
                                authentication = shortcut.authenticationType?.type,
                                launcherShortcut = shortcut.launcherShortcut.falseOrNull(),
                                secondaryLauncherShortcut = shortcut.secondaryLauncherShortcut.trueOrNull(),
                                quickSettingsTileShortcut = shortcut.quickSettingsTileShortcut.trueOrNull(),
                                delay = shortcut.delay.takeIf { it != 0 },
                                repetitionInterval = shortcut.repetitionInterval,
                                requestBodyType = shortcut.takeIf { type == HTTP }
                                    ?.requestBodyType
                                    ?.takeIf { it != RequestBodyType.CUSTOM_TEXT }?.type,
                                contentType = shortcut.takeIf { type == HTTP }
                                    ?.contentType
                                    ?.takeUnlessEmpty(),
                                responseHandling = if (type == HTTP) {
                                    ExportResponseHandling(
                                        actions = shortcut.responseDisplayActions
                                            .takeUnlessEmpty()
                                            ?.map { it.key },
                                        uiType = shortcut.responseUiType
                                            .takeIf { it != ResponseUiType.WINDOW }
                                            ?.type,
                                        successOutput = shortcut.responseSuccessOutput
                                            .takeIf { it != ResponseSuccessOutput.RESPONSE }
                                            ?.type,
                                        failureOutput = shortcut.responseFailureOutput
                                            .takeIf { it != ResponseFailureOutput.DETAILED }
                                            ?.type,
                                        contentType = shortcut.responseContentType?.key,
                                        charset = shortcut.responseCharset?.name(),
                                        successMessage = shortcut.responseSuccessMessage
                                            .takeUnlessEmpty()
                                            ?.takeIf { shortcut.responseSuccessOutput == ResponseSuccessOutput.MESSAGE },
                                        includeMetaInfo = shortcut.responseIncludeMetaInfo.trueOrNull(),
                                        jsonArrayAsTable = shortcut.responseJsonArrayAsTable.falseOrNull(),
                                        monospace = shortcut.responseMonospace.trueOrNull(),
                                        fontSize = shortcut.responseFontSize,
                                        javaScriptEnabled = shortcut.responseJavaScriptEnabled.trueOrNull(),
                                        storeDirectoryId = shortcut.responseStoreDirectoryId,
                                        storeFileName = shortcut.responseStoreFileName?.takeUnlessEmpty(),
                                        replaceFileIfExists = shortcut.responseReplaceFileIfExists.trueOrNull(),
                                    )
                                } else {
                                    null
                                },
                                fileUploadOptions = if (type == HTTP && shortcut.requestBodyType == RequestBodyType.FILE) {
                                    ExportFileUploadOptions(
                                        fileUploadType = shortcut.fileUploadType?.type,
                                        file = shortcut.fileUploadSourceFile,
                                        useImageEditor = shortcut.fileUploadUseImageEditor.trueOrNull(),
                                    )
                                } else {
                                    null
                                },
                                confirmation = shortcut.confirmationType?.type,
                                followRedirects = shortcut
                                    .takeIf { type == HTTP }
                                    ?.followRedirects
                                    ?.falseOrNull(),
                                acceptCookies = shortcut
                                    .takeIf { type == HTTP }
                                    ?.acceptCookies
                                    ?.falseOrNull(),
                                keepConnectionOpen = shortcut
                                    .takeIf { type == HTTP }
                                    ?.keepConnectionOpen
                                    ?.trueOrNull(),
                                protocolVersion = shortcut
                                    .takeIf { type == HTTP }
                                    ?.ipVersion?.version,
                                proxy = shortcut
                                    .takeIf { type == HTTP }
                                    ?.proxyType?.type,
                                proxyHost = shortcut
                                    .takeIf { type == HTTP }
                                    ?.proxyHost?.takeUnlessEmpty(),
                                proxyPort = shortcut
                                    .takeIf { type == HTTP }
                                    ?.proxyPort,
                                proxyUsername = shortcut
                                    .takeIf { type == HTTP }
                                    ?.proxyUsername
                                    ?.takeUnlessEmpty(),
                                proxyPassword = shortcut
                                    .takeIf { type == HTTP }
                                    ?.proxyPassword
                                    ?.takeUnlessEmpty(),
                                wifiSsid = shortcut
                                    .takeIf { type == HTTP }
                                    ?.wifiSsid
                                    ?.takeUnlessEmpty(),
                                clientCert = shortcut
                                    .takeIf { type == HTTP }
                                    ?.clientCertParams
                                    ?.toString(),
                                codeOnPrepare = shortcut.codeOnPrepare.takeUnlessEmpty(),
                                codeOnSuccess = shortcut.codeOnSuccess.takeUnlessEmpty(),
                                codeOnFailure = shortcut.codeOnFailure.takeUnlessEmpty(),
                                browserPackageName = shortcut
                                    .takeIf { type == ShortcutExecutionType.BROWSER }
                                    ?.targetBrowser
                                    ?.serialize(),
                                excludeFromHistory = shortcut.excludeFromHistory.trueOrNull(),
                                excludeFromFileSharing = shortcut.excludeFromFileSharing.trueOrNull(),
                                runInForegroundService = shortcut.runInForegroundService.trueOrNull(),
                                wolMacAddress = shortcut.takeIf { type == ShortcutExecutionType.WAKE_ON_LAN }
                                    ?.wolMacAddress,
                                wolPort = shortcut.takeIf { type == ShortcutExecutionType.WAKE_ON_LAN }
                                    ?.wolPort
                                    ?.takeIf { it != 9 },
                                wolBroadcastAddress = shortcut.takeIf { type == ShortcutExecutionType.WAKE_ON_LAN }
                                    ?.wolBroadcastAddress
                                    ?.takeIf { it != "255.255.255.255" },
                                headers = requestHeadersByShortcutId[shortcut.id]
                                    ?.takeIf { type == HTTP }
                                    ?.takeUnlessEmpty()
                                    ?.map { header ->
                                        ExportHeader(
                                            key = header.key,
                                            value = header.value,
                                        )
                                    },
                                parameters = requestParametersByShortcutId[shortcut.id]
                                    ?.takeIf { type == HTTP }
                                    ?.takeUnlessEmpty()
                                    ?.map { parameter ->
                                        ExportParameter(
                                            key = parameter.key,
                                            value = parameter.value
                                                .takeIf { parameter.parameterType == ParameterType.STRING },
                                            fileName = parameter.fileUploadFileName
                                                ?.takeUnlessEmpty()
                                                ?.takeIf { parameter.parameterType == ParameterType.FILE },
                                            type = parameter.parameterType.takeIf { it != ParameterType.STRING }?.type,
                                            fileUploadOptions = if (parameter.parameterType == ParameterType.FILE) {
                                                ExportFileUploadOptions(
                                                    fileUploadType = parameter.fileUploadType?.type,
                                                    file = parameter.fileUploadSourceFile,
                                                    useImageEditor = parameter.fileUploadUseImageEditor.trueOrNull(),
                                                )
                                                    .takeIf { it.fileUploadType != null || it.file != null || it.useImageEditor != null }
                                            } else {
                                                null
                                            },
                                        )
                                    },
                            )
                        },
                )
            },
            variables = variables
                .takeUnlessEmpty()
                ?.map { variable ->
                    ExportVariable(
                        id = variable.id,
                        key = variable.key,
                        type = variable.type
                            .takeIf { it != VariableType.CONSTANT }
                            ?.type,
                        value = variable.value?.takeUnlessEmpty()?.takeUnless { excludeVariableValuesIfNeeded },
                        data = variable.data?.takeUnless { it.isEmpty() || it == "{}" },
                        rememberValue = variable.rememberValue.trueOrNull(),
                        urlEncode = variable.urlEncode.trueOrNull(),
                        jsonEncode = variable.jsonEncode.trueOrNull(),
                        title = variable.title.takeUnlessEmpty(),
                        message = variable.message.takeUnlessEmpty(),
                        isShareText = variable.isShareText.trueOrNull(),
                        isShareTitle = variable.isShareTitle.trueOrNull(),
                        isMultiline = variable.isMultiline.trueOrNull(),
                        isExcludeValueFromExport = variable.isExcludeValueFromExport.trueOrNull(),
                    )
                },
            certificatePins = certificatePinRepository.getCertificatePins()
                .takeUnlessEmpty()
                ?.map { pin ->
                    ExportCertificatePin(
                        id = pin.id,
                        pattern = pin.pattern,
                        hash = pin.hash,
                    )
                },
            workingDirectories = workingDirectoryRepository.getWorkingDirectories()
                .filter {
                    it.id in relevantWorkingDirectoryIds
                }
                .takeUnlessEmpty()
                ?.map { directory ->
                    ExportWorkingDirectory(
                        id = directory.id,
                        name = directory.name,
                        directory = directory.directory.toString(),
                    )
                },
            title = appConfig.title.takeUnlessEmpty(),
            globalCode = appConfig.globalCode.takeUnlessEmpty(),
        )
    }
}
