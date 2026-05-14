package ch.rmy.android.http_shortcuts.data.domains.import_export

import androidx.core.net.toUri
import ch.rmy.android.framework.extensions.isSubSequenceOf
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.extensions.toCharset
import ch.rmy.android.framework.extensions.truncate
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.Constants
import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.domains.BaseRepository
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
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
import ch.rmy.android.http_shortcuts.data.enums.ResponseDisplayAction
import ch.rmy.android.http_shortcuts.data.enums.ResponseFailureOutput
import ch.rmy.android.http_shortcuts.data.enums.ResponseSuccessOutput
import ch.rmy.android.http_shortcuts.data.enums.ResponseUiType
import ch.rmy.android.http_shortcuts.data.enums.SecurityPolicy
import ch.rmy.android.http_shortcuts.data.enums.ShortcutAuthenticationType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutClickBehavior
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.enums.VariableType
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.CertificatePin
import ch.rmy.android.http_shortcuts.data.models.GlobalVariable
import ch.rmy.android.http_shortcuts.data.models.RequestHeader
import ch.rmy.android.http_shortcuts.data.models.RequestParameter
import ch.rmy.android.http_shortcuts.data.models.Section
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.WorkingDirectory
import ch.rmy.android.http_shortcuts.data.settings.DeviceLocalPreferences
import ch.rmy.android.http_shortcuts.extensions.ids
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.import_export.ImportMode
import ch.rmy.android.http_shortcuts.import_export.models.ImportBase
import ch.rmy.android.http_shortcuts.import_export.models.ImportCategory
import ch.rmy.android.http_shortcuts.import_export.models.ImportCertificatePin
import ch.rmy.android.http_shortcuts.import_export.models.ImportHeader
import ch.rmy.android.http_shortcuts.import_export.models.ImportParameter
import ch.rmy.android.http_shortcuts.import_export.models.ImportSection
import ch.rmy.android.http_shortcuts.import_export.models.ImportShortcut
import ch.rmy.android.http_shortcuts.import_export.models.ImportVariable
import ch.rmy.android.http_shortcuts.import_export.models.ImportWorkingDirectory
import ch.rmy.android.http_shortcuts.variables.types.SelectType
import javax.inject.Inject

class ImportRepository
@Inject
constructor(
    database: Database,
    private val deviceLocalPreferences: DeviceLocalPreferences,
) : BaseRepository(database) {
    suspend fun import(base: ImportBase, mode: ImportMode) = commitTransaction {
        val existingVariables = globalVariableDao().getVariables()
        if (mode == ImportMode.REPLACE) {
            categoryDao().deleteAllCategories()
            sectionDao().deleteAllSections()
            shortcutDao().deleteAllShortcuts()
            requestHeaderDao().deleteAllRequestHeaders()
            requestParameterDao().deleteAllRequestParameters()
            globalVariableDao().deleteAll()
            certificatePinDao().deleteAllCertificatePins()
            workingDirectoryDao().deleteAllWorkingDirectories()
        }
        importAppConfig(base, mode)
        importCategories(base.categories ?: emptyList(), mode)
        importVariables(base.variables ?: emptyList(), mode, existingVariables)
        importCertificatePins(base.certificatePins ?: emptyList())
        importWorkingDirectories(base.workingDirectories ?: emptyList(), fromSameDevice = base.originDeviceId == deviceLocalPreferences.deviceId)
        validate()
    }

    private suspend fun Database.importAppConfig(importBase: ImportBase, mode: ImportMode) {
        appConfigDao()
            .update { oldAppConfig ->
                var newTitle = oldAppConfig.title
                var newGlobalCode = oldAppConfig.globalCode
                when (mode) {
                    ImportMode.MERGE -> {
                        if (!importBase.title.isNullOrEmpty() && oldAppConfig.title.isEmpty()) {
                            newTitle = importBase.title
                        }
                        if (!importBase.globalCode.isNullOrEmpty() && oldAppConfig.globalCode.isEmpty()) {
                            newGlobalCode = importBase.globalCode
                        }
                    }
                    ImportMode.REPLACE -> {
                        newTitle = importBase.title ?: ""
                        newGlobalCode = importBase.globalCode ?: ""
                    }
                }
                oldAppConfig.copy(
                    title = newTitle,
                    globalCode = newGlobalCode,
                )
            }
    }

    private suspend fun Database.importCategories(importCategories: List<ImportCategory>, mode: ImportMode) {
        val categories = importCategories.mapIndexed { index, category ->
            val categoryId = category.id ?: newUUID()
            importSections(categoryId, category.sections ?: emptyList(), mode)
            importShortcuts(categoryId, category.shortcuts ?: emptyList(), mode)

            Category(
                id = categoryId,
                name = category.name!!.truncate(Constants.CATEGORY_NAME_MAX_LENGTH),
                icon = category.iconName?.let { ShortcutIcon.fromName(it) },
                layoutType = category.layoutType?.let { CategoryLayoutType.parse(it) } ?: CategoryLayoutType.LINEAR_LIST,
                background = category.background?.let { CategoryBackgroundType.parse(it) } ?: CategoryBackgroundType.Default,
                hidden = category.hidden == true,
                hiddenLabels = category.hiddenLabels == true,
                scale = category.scale?.coerceIn(0.5f, 4f) ?: 1f,
                shortcutClickBehavior = category.shortcutClickBehavior?.let { ShortcutClickBehavior.parse(it) },
                sortingOrder = index,
            )
        }

        val categoryDao = categoryDao()
        when (mode) {
            ImportMode.MERGE -> {
                val existingCategories = categoryDao.getCategories()

                val singleCategory = existingCategories.singleOrNull()
                if (
                    singleCategory != null &&
                    sectionDao().getSectionByCategoryId(singleCategory.id).isEmpty() &&
                    shortcutDao().getShortcutsByCategoryId(singleCategory.id).isEmpty()
                ) {
                    categoryDao.deleteCategory(singleCategory.id)
                    categories.forEach { category ->
                        categoryDao.insertOrUpdateCategory(category)
                    }
                    return
                }

                val newCategoriesById = categories.associateBy { it.id }

                existingCategories.forEach { category ->
                    newCategoriesById[category.id]?.let { newCategory ->
                        categoryDao.insertOrUpdateCategory(
                            newCategory.copy(
                                sortingOrder = category.sortingOrder,
                            ),
                        )
                    }
                }

                var sortingOrder = existingCategories.size
                val existingCategoryIds = existingCategories.ids().toSet()
                categories
                    .filter { it.id !in existingCategoryIds }
                    .forEach { category ->
                        categoryDao.insertOrUpdateCategory(
                            category.copy(
                                sortingOrder = sortingOrder,
                            ),
                        )
                        sortingOrder++
                    }
            }
            ImportMode.REPLACE -> {
                categories.forEach { category ->
                    categoryDao.insertOrUpdateCategory(category)
                }
            }
        }
    }

    private suspend fun Database.importSections(categoryId: CategoryId, importSections: List<ImportSection>, mode: ImportMode) {
        val sections = importSections.mapIndexed { index, section ->
            Section(
                id = section.id ?: newUUID(),
                categoryId = categoryId,
                name = section.name!!,
                sortingOrder = index,
            )
        }

        val sectionDao = sectionDao()
        when (mode) {
            ImportMode.MERGE -> {
                val existingSections = sectionDao.getSectionByCategoryId(categoryId)
                val newSectionsById = sections.associateBy { it.id }

                existingSections.forEach { section ->
                    newSectionsById[section.id]?.let { newSection ->
                        sectionDao.insertOrUpdateSection(
                            newSection.copy(
                                sortingOrder = section.sortingOrder,
                            ),
                        )
                    }
                }

                var sortingOrder = existingSections.size
                val existingSectionIds = existingSections.ids().toSet()
                sections
                    .filter { it.id !in existingSectionIds }
                    .forEach { section ->
                        sectionDao.insertOrUpdateSection(
                            section.copy(
                                sortingOrder = sortingOrder,
                            ),
                        )
                        sortingOrder++
                    }
            }
            ImportMode.REPLACE -> {
                sections.forEach { section ->
                    sectionDao.insertOrUpdateSection(section)
                }
            }
        }
    }

    private suspend fun Database.importShortcuts(categoryId: CategoryId, importShortcuts: List<ImportShortcut>, mode: ImportMode) {
        val shortcuts = importShortcuts.mapIndexed { index, shortcut ->
            Shortcut(
                id = shortcut.id ?: newUUID(),
                executionType = ShortcutExecutionType.parse(shortcut.executionType!!) ?: ShortcutExecutionType.HTTP,
                categoryId = categoryId,
                name = shortcut.name!!.truncate(Constants.SHORTCUT_NAME_MAX_LENGTH),
                description = shortcut.description?.truncate(Constants.SHORTCUT_DESCRIPTION_MAX_LENGTH) ?: "",
                icon = ShortcutIcon.fromName(shortcut.iconName),
                hidden = shortcut.hidden == true,
                method = HttpMethod.parse(shortcut.method!!) ?: HttpMethod.GET,
                url = shortcut.url!!,
                authenticationType = shortcut.authentication?.let { ShortcutAuthenticationType.parse(it) },
                authUsername = shortcut.username ?: "",
                authPassword = shortcut.password ?: "",
                authToken = shortcut.authToken ?: "",
                sectionId = shortcut.section,
                bodyContent = shortcut.bodyContent ?: "",
                timeout = shortcut.timeout!!,
                isWaitForNetwork = shortcut.waitForInternet == true,
                securityPolicy = when {
                    shortcut.acceptAllCertificates == true -> SecurityPolicy.AcceptAll
                    !shortcut.certificateFingerprint.isNullOrEmpty() -> SecurityPolicy.FingerprintOnly(shortcut.certificateFingerprint)
                    else -> null
                },
                launcherShortcut = shortcut.launcherShortcut == true,
                secondaryLauncherShortcut = shortcut.secondaryLauncherShortcut == true,
                quickSettingsTileShortcut = shortcut.quickSettingsTileShortcut == true,
                delay = shortcut.delay ?: 0,
                repetitionInterval = shortcut.repetitionInterval?.takeIf { it > 0 },
                contentType = shortcut.contentType ?: "",
                fileUploadType = shortcut.fileUploadOptions
                    ?.fileUploadType
                    ?.let { FileUploadType.parse(it) }
                    ?.let {
                        if (it == FileUploadType.STATIC_VALUE) {
                            FileUploadType.FILE_PICKER
                        } else {
                            it
                        }
                    },
                fileUploadSourceDirectoryId = shortcut.fileUploadOptions?.directoryId,
                fileUploadSourceFileName = shortcut.fileUploadOptions?.fileName,
                fileUploadUseImageEditor = shortcut.fileUploadOptions?.useImageEditor == true,
                confirmationType = shortcut.confirmation?.let { ConfirmationType.parse(it) },
                followRedirects = shortcut.followRedirects == true,
                acceptCookies = shortcut.acceptCookies == true,
                keepConnectionOpen = shortcut.keepConnectionOpen == true,
                wifiSsid = shortcut.wifiSsid,
                codeOnPrepare = shortcut.codeOnPrepare ?: "",
                codeOnSuccess = shortcut.codeOnSuccess ?: "",
                codeOnFailure = shortcut.codeOnFailure ?: "",
                targetBrowser = shortcut.browserPackageName?.let { TargetBrowser.parse(it) } ?: TargetBrowser.Browser(null),
                excludeFromHistory = shortcut.excludeFromHistory == true,
                clientCertParams = shortcut.clientCert?.let { ClientCertParams.parse(it) },
                requestBodyType = RequestBodyType.parse(shortcut.requestBodyType!!) ?: RequestBodyType.CUSTOM_TEXT,
                ipVersion = shortcut.protocolVersion?.let { IpVersion.parse(it) },
                proxyType = shortcut.proxy?.let { ProxyType.parse(it) },
                proxyHost = shortcut.proxyHost,
                proxyPort = shortcut.proxyPort,
                proxyUsername = shortcut.proxyUsername,
                proxyPassword = shortcut.proxyPassword,
                excludeFromFileSharing = shortcut.excludeFromFileSharing == true,
                runInForegroundService = shortcut.runInForegroundService == true,
                wolMacAddress = shortcut.wolMacAddress ?: "",
                wolPort = shortcut.wolPort!!,
                wolBroadcastAddress = shortcut.wolBroadcastAddress ?: "",
                responseActions = shortcut.responseHandling
                    ?.actions
                    ?.filter { ResponseDisplayAction.parse(it) != null }
                    ?.distinct()
                    ?.joinToString(separator = ",")
                    ?: "",
                responseUiType = shortcut.responseHandling
                    ?.uiType
                    ?.let { ResponseUiType.parse(it) }
                    ?: ResponseUiType.WINDOW,
                responseSuccessOutput = shortcut.responseHandling
                    ?.successOutput
                    ?.let { ResponseSuccessOutput.parse(it) }
                    ?: ResponseSuccessOutput.RESPONSE,
                responseFailureOutput = shortcut.responseHandling
                    ?.failureOutput
                    ?.let { ResponseFailureOutput.parse(it) }
                    ?: ResponseFailureOutput.DETAILED,
                responseContentType = shortcut.responseHandling
                    ?.contentType
                    ?.let { ResponseContentType.parse(it) },
                responseCharset = shortcut.responseHandling
                    ?.charset
                    ?.toCharset(),
                responseSuccessMessage = shortcut.responseHandling
                    ?.successMessage
                    ?: "",
                responseIncludeMetaInfo = shortcut.responseHandling?.includeMetaInfo == true,
                responseJsonArrayAsTable = shortcut.responseHandling?.jsonArrayAsTable == true,
                responseMonospace = shortcut.responseHandling?.monospace == true,
                responseFontSize = shortcut.responseHandling?.fontSize,
                responseJavaScriptEnabled = shortcut.responseHandling?.javaScriptEnabled == true,
                responseStoreDirectoryId = shortcut.responseHandling?.storeDirectoryId,
                responseStoreFileName = shortcut.responseHandling?.storeFileName,
                responseReplaceFileIfExists = shortcut.responseHandling?.replaceFileIfExists == true,
                sortingOrder = index,
            )
        }

        val shortcutDao = shortcutDao()
        when (mode) {
            ImportMode.MERGE -> {
                val existingShortcuts = shortcutDao.getShortcutsByCategoryId(categoryId)
                val existingShortcutIdsList = existingShortcuts.ids()
                val importedShortcutIdsList = shortcuts.ids()
                val desiredOrder = determineSortingOrder(existingShortcutIdsList, importedShortcutIdsList)

                // Store new shortcuts and updates to existing shortcuts
                shortcuts.forEach { shortcut ->
                    shortcutDao.insertOrUpdateShortcut(
                        shortcut.copy(
                            sortingOrder = desiredOrder[shortcut.id]!!,
                        ),
                    )
                }

                // Update sorting order of shortcuts that have otherwise not been changed by the import
                val importedShortcutIdsSet = importedShortcutIdsList.toSet()
                existingShortcuts
                    .filter { it.id !in importedShortcutIdsSet }
                    .forEach { shortcut ->
                        shortcutDao.insertOrUpdateShortcut(
                            shortcut.copy(
                                sortingOrder = desiredOrder[shortcut.id]!!,
                            ),
                        )
                    }
            }
            ImportMode.REPLACE -> {
                shortcuts.forEach { shortcut ->
                    shortcutDao.insertOrUpdateShortcut(shortcut)
                }
            }
        }

        importShortcuts.forEachIndexed { index, shortcut ->
            if (!shortcut.headers.isNullOrEmpty() || !shortcut.parameters.isNullOrEmpty()) {
                val shortcutId = shortcut.id ?: shortcuts[index].id
                shortcut.headers?.takeUnlessEmpty()?.let { headers ->
                    importRequestHeaders(shortcutId, headers)
                }
                shortcut.parameters?.takeUnlessEmpty()?.let { parameters ->
                    importRequestParameters(shortcutId, parameters)
                }
            }
        }
    }

    private fun determineSortingOrder(
        existingShortcutIdsList: List<ShortcutId>,
        importedShortcutIdsList: List<ShortcutId>,
    ): Map<ShortcutId, Int> =
        when {
            importedShortcutIdsList.isSubSequenceOf(existingShortcutIdsList) -> {
                existingShortcutIdsList
            }
            existingShortcutIdsList.isSubSequenceOf(importedShortcutIdsList) -> {
                importedShortcutIdsList
            }
            existingShortcutIdsList.toSet() == importedShortcutIdsList.toSet() -> {
                importedShortcutIdsList
            }
            else -> {
                buildList {
                    // Keep the existing shortcuts at the beginning
                    addAll(existingShortcutIdsList)

                    // Append new shortcuts at the end
                    val existingShortcutIdsSet = existingShortcutIdsList.toSet()
                    addAll(importedShortcutIdsList.filter { it !in existingShortcutIdsSet })
                }
            }
        }
            .mapIndexed { index, shortcutId -> shortcutId to index }
            .toMap()

    private suspend fun Database.importRequestHeaders(shortcutId: ShortcutId, importHeaders: List<ImportHeader>) {
        val requestHeaderDao = requestHeaderDao()
        val headers = importHeaders.mapIndexed { index, header ->
            RequestHeader(
                shortcutId = shortcutId,
                key = header.key!!,
                value = header.value ?: "",
                sortingOrder = index,
            )
        }
        requestHeaderDao.deleteRequestHeaderByShortcutId(shortcutId)
        headers.forEach { header ->
            requestHeaderDao.insertOrUpdateRequestHeader(header)
        }
    }

    private suspend fun Database.importRequestParameters(shortcutId: ShortcutId, importParameters: List<ImportParameter>) {
        val requestParameterDao = requestParameterDao()
        val parameters = importParameters.mapIndexed { index, parameter ->
            val type = parameter.type?.let { ParameterType.parse(it) } ?: ParameterType.STRING
            val fileUploadOptions = parameter.fileUploadOptions?.takeIf { type == ParameterType.FILE }
            RequestParameter(
                shortcutId = shortcutId,
                key = parameter.key!!,
                value = when {
                    type == ParameterType.STRING -> parameter.value
                    fileUploadOptions?.fileUploadType == FileUploadType.STATIC_VALUE.type -> fileUploadOptions.value
                    else -> null
                }
                    .orEmpty(),
                parameterType = type,
                fileUploadType = fileUploadOptions?.fileUploadType?.let { FileUploadType.parse(it) },
                fileUploadFileName = parameter.fileName?.takeIf { type == ParameterType.FILE },
                fileUploadSourceDirectoryId = fileUploadOptions?.directoryId,
                fileUploadSourceFileName = fileUploadOptions?.fileName,
                fileUploadUseImageEditor = fileUploadOptions?.useImageEditor == true,
                sortingOrder = index,
            )
        }
        requestParameterDao.deleteRequestParametersByShortcutId(shortcutId)
        parameters.forEach { parameter ->
            requestParameterDao.insertOrUpdateRequestParameter(parameter)
        }
    }

    private suspend fun Database.importVariables(importVariables: List<ImportVariable>, mode: ImportMode, existingVariables: List<GlobalVariable>) {
        val variables = importVariables.mapIndexed { index, variable ->
            GlobalVariable(
                id = variable.id ?: newUUID(),
                key = variable.key!!,
                type = variable.type?.let { VariableType.parse(it) } ?: VariableType.CONSTANT,
                value = if (variable.isExcludeValueFromExport == true && variable.value.isNullOrEmpty()) {
                    val oldVariable = existingVariables.find { it.id == variable.id }
                    if (oldVariable != null && oldVariable.isExcludeValueFromExport) {
                        oldVariable.value
                    } else {
                        variable.value
                    }
                } else {
                    variable.value
                },
                data = variable.data,
                rememberValue = variable.rememberValue == true,
                urlEncode = variable.urlEncode == true,
                jsonEncode = variable.jsonEncode == true,
                title = variable.title ?: "",
                message = variable.message ?: "",
                isShareText = variable.isShareText == true,
                isShareTitle = variable.isShareTitle == true,
                isMultiline = variable.isMultiline == true,
                isExcludeValueFromExport = variable.isExcludeValueFromExport == true,
                isSecret = variable.isSecret == true,
                sortingOrder = index,
            )
        }
        with(globalVariableDao()) {
            when (mode) {
                ImportMode.MERGE -> {
                    val newVariablesById = variables.associateBy { it.id }
                    existingVariables.forEach { variable ->
                        newVariablesById[variable.id]?.let { newVariable ->
                            insertOrUpdateVariable(
                                newVariable.copy(
                                    value = if (
                                        variable.isExcludeValueFromExport &&
                                        newVariable.isExcludeValueFromExport &&
                                        newVariable.value.isNullOrEmpty()
                                    ) {
                                        variable.value
                                    } else {
                                        newVariable.value
                                    },
                                    sortingOrder = variable.sortingOrder,
                                ),
                            )
                        }
                    }

                    var sortingOrder = existingVariables.size
                    val existingVariableIds = existingVariables.ids().toSet()
                    variables
                        .filter { it.id !in existingVariableIds }
                        .forEach { variable ->
                            insertOrUpdateVariable(
                                variable.copy(
                                    sortingOrder = sortingOrder,
                                ),
                            )
                            sortingOrder++
                        }
                }
                ImportMode.REPLACE -> {
                    variables.forEach { variable ->
                        insertOrUpdateVariable(variable)
                    }
                }
            }
        }
    }

    private suspend fun Database.importCertificatePins(importPins: List<ImportCertificatePin>) {
        val pins = importPins.map { pin ->
            CertificatePin(
                id = pin.id ?: newUUID(),
                pattern = pin.pattern!!,
                hash = pin.hash!!,
            )
        }

        val certificatePinDao = certificatePinDao()
        pins.forEach { pin ->
            certificatePinDao.insertCertificatePin(pin)
        }
    }

    private suspend fun Database.importWorkingDirectories(importWorkingDirectories: List<ImportWorkingDirectory>, fromSameDevice: Boolean) {
        val workingDirectoryDao = workingDirectoryDao()
        val existingWorkingDirectoriesById = workingDirectoryDao.getWorkingDirectories().associateBy { it.id }
        val workingDirectories = importWorkingDirectories.map { workingDirectory ->
            val existingWorkingDirectory = existingWorkingDirectoriesById[workingDirectory.id]
            WorkingDirectory(
                id = workingDirectory.id ?: newUUID(),
                name = workingDirectory.name!!,
                directory = if (!fromSameDevice && existingWorkingDirectory != null) {
                    existingWorkingDirectory.directory
                } else {
                    workingDirectory.directory!!.toUri()
                },
                accessed = existingWorkingDirectory?.accessed,
            )
        }
        workingDirectories.forEach { workingDirectory ->
            workingDirectoryDao.insertOrUpdateWorkingDirectory(workingDirectory)
        }
    }

    private suspend fun Database.validate() {
        val categories = categoryDao().getCategories()
        require(categories.isNotEmpty() && categories.any { !it.hidden }) {
            "There must be at least one non-hidden category"
        }
        val variables = globalVariableDao().getVariables()
        variables.forEach { variable ->
            if (variable.type == VariableType.SELECT) {
                val values = variable.getStringListData(SelectType.KEY_VALUES)
                require(!values.isNullOrEmpty()) { "'select' variable without options found" }
            }
        }
    }
}
