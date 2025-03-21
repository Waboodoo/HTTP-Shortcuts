package ch.rmy.android.http_shortcuts.data.domains.app

import ch.rmy.android.framework.data.BaseRealmRepository
import ch.rmy.android.framework.data.RealmFactory
import ch.rmy.android.framework.data.RealmTransactionContext
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.domains.getBase
import ch.rmy.android.http_shortcuts.data.domains.getTemporaryShortcut
import ch.rmy.android.http_shortcuts.data.domains.getTemporaryVariable
import ch.rmy.android.http_shortcuts.data.models.AppLock
import ch.rmy.android.http_shortcuts.data.models.Base
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.Header
import ch.rmy.android.http_shortcuts.data.models.Option
import ch.rmy.android.http_shortcuts.data.models.Parameter
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.import_export.ImportExportBase
import ch.rmy.android.http_shortcuts.import_export.Importer
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class AppRepository
@Inject
constructor(
    database: Database,
    realmFactory: RealmFactory,
) : BaseRealmRepository(database, realmFactory) {
    suspend fun getBase(): Base =
        queryItem {
            getBase()
        }

    suspend fun getGlobalCode(): String =
        query {
            getBase()
        }
            .firstOrNull()
            ?.globalCode
            .orEmpty()

    suspend fun getToolbarTitle(): String =
        queryItem {
            getBase()
        }
            .title
            ?.takeUnless { it.isBlank() }
            .orEmpty()

    fun getObservableToolbarTitle(): Flow<String> =
        observeItem {
            getBase()
        }
            .map { base ->
                base.title
                    ?.takeUnless { it.isBlank() }
                    .orEmpty()
            }

    suspend fun setToolbarTitle(title: String) {
        commitTransaction {
            getBase()
                .findFirst()
                ?.title = title
        }
    }

    suspend fun setGlobalCode(globalCode: String?) {
        commitTransaction {
            getBase()
                .findFirst()
                ?.let { base ->
                    base.globalCode = globalCode
                }
        }
    }

    suspend fun getLock(): AppLock? =
        get(Database::appLockDao)
            .getAppLock()

    fun getObservableLock(): Flow<AppLock?> =
        flow(Database::appLockDao) {
            observeAppLock()
                .distinctUntilChanged()
        }

    suspend fun setLock(passwordHash: String, useBiometrics: Boolean) {
        get(Database::appLockDao)
            .insert(
                AppLock(
                    passwordHash = passwordHash,
                    useBiometrics = useBiometrics,
                ),
            )
    }

    suspend fun removeLock() {
        get(Database::appLockDao).deleteAppLock()
    }

    suspend fun importBase(base: ImportExportBase, importMode: Importer.ImportMode) {
        commitTransaction {
            logInfo("Importing base ($importMode)")
            val oldBase = getBase().findFirst()!!
            when (importMode) {
                Importer.ImportMode.MERGE -> {
                    if (base.title != null && oldBase.title.isNullOrEmpty()) {
                        oldBase.title = base.title
                    }
                    if (base.globalCode != null && oldBase.globalCode.isNullOrEmpty()) {
                        oldBase.globalCode = base.globalCode
                    }

                    if (oldBase.categories.singleOrNull()?.shortcuts?.isEmpty() == true) {
                        oldBase.categories.singleOrNull()?.delete()
                        oldBase.categories.clear()
                    }

                    base.categories.forEach { category ->
                        importCategory(oldBase, category)
                    }

                    val persistedVariables = copyOrUpdate(base.variables.distinctBy { it.id })
                    val persistedVariablesIds = persistedVariables.map { it.id }
                    oldBase.variables.removeIf { it.id in persistedVariablesIds }
                    oldBase.variables.addAll(persistedVariables)

                    val persistedWorkingDirectories = copyOrUpdate(base.workingDirectories)
                    val persistedWorkingDirectoryIds = persistedWorkingDirectories.map { it.id }
                    oldBase.workingDirectories.removeIf { it.id in persistedWorkingDirectoryIds }
                    oldBase.workingDirectories.addAll(persistedWorkingDirectories)
                }
                Importer.ImportMode.REPLACE -> {
                    if (base.title != null) {
                        oldBase.title = base.title
                    }
                    if (base.globalCode != null) {
                        oldBase.globalCode = base.globalCode
                    }

                    oldBase.categories.clear()
                    oldBase.categories.addAll(copyOrUpdate(base.categories))

                    oldBase.variables.clear()
                    oldBase.variables.addAll(copyOrUpdate(base.variables))

                    oldBase.workingDirectories.clear()
                    oldBase.workingDirectories.addAll(copyOrUpdate(base.workingDirectories))
                }
            }
            oldBase.validate()
        }
    }

    private fun RealmTransactionContext.importCategory(base: Base, category: Category) {
        val oldCategory = base.categories.find { it.id == category.id }
        if (oldCategory == null) {
            base.categories.add(copyOrUpdate(category))
        } else {
            oldCategory.name = category.name
            oldCategory.categoryBackgroundType = category.categoryBackgroundType
            oldCategory.hidden = category.hidden
            oldCategory.categoryLayoutType = category.categoryLayoutType
            category.shortcuts.forEach { shortcut ->
                importShortcut(oldCategory, shortcut)
            }
        }
    }

    private fun RealmTransactionContext.importShortcut(category: Category, shortcut: Shortcut) {
        val oldShortcut = category.shortcuts.find { it.id == shortcut.id }
        if (oldShortcut == null) {
            category.shortcuts.add(copyOrUpdate(shortcut))
        } else {
            copyOrUpdate(shortcut)
        }
    }

    suspend fun deleteUnusedData() {
        commitTransaction {
            val base = getBase().findFirst() ?: return@commitTransaction
            val temporaryShortcut = getTemporaryShortcut().findFirst()
            val temporaryVariable = getTemporaryVariable().findFirst()
            val categories = base.categories
            val shortcuts = base.shortcuts
                .runIfNotNull(temporaryShortcut) {
                    plus(it)
                }
            val variables = base.variables.toList()
                .runIfNotNull(temporaryVariable) {
                    plus(it)
                }

            // Delete orphaned categories
            val usedCategoryIds = categories.map { it.id }
            get<Category>()
                .find()
                .filter {
                    // TODO: Use RealmQL for this
                    it.id !in usedCategoryIds
                }
                .deleteAll()

            // Delete orphaned shortcuts
            val usedShortcutIds = shortcuts.map { it.id }
            get<Shortcut>("${Shortcut.FIELD_ID} != $0", Shortcut.TEMPORARY_ID)
                .find()
                .filter {
                    // TODO: Use RealmQL for this
                    it.id !in usedShortcutIds
                }
                .deleteAll()

            // Delete orphaned headers
            val usedHeaderIds = shortcuts
                .flatMap { it.headers }
                .map { header -> header.id }
            get<Header>()
                .find()
                .filter {
                    // TODO: Use RealmQL for this
                    it.id !in usedHeaderIds
                }
                .deleteAll()

            // Delete orphaned parameters
            val usedParameterIds = shortcuts
                .flatMap { it.parameters }
                .map { parameter -> parameter.id }
            get<Parameter>()
                .find()
                .filter {
                    // TODO: Use RealmQL for this
                    it.id !in usedParameterIds
                }
                .deleteAll()

            // Delete orphaned variables
            val usedVariableIds = variables.map { it.id }
            get<Variable>("${Variable.FIELD_ID} != $0", Variable.TEMPORARY_ID)
                .find()
                .filter {
                    // TODO: Use RealmQL for this
                    it.id !in usedVariableIds
                }
                .deleteAll()

            // Delete orphaned options
            val usedOptionIds = variables.flatMap { it.options ?: emptyList() }.map { it.id }
            get<Option>()
                .find()
                .filter {
                    // TODO: Use RealmQL for this
                    it.id !in usedOptionIds
                }
                .deleteAll()

            // TODO: Delete orphaned certificate pins
        }
    }
}
