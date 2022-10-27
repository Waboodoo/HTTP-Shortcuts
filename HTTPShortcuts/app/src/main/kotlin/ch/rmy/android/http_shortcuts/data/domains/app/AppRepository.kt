package ch.rmy.android.http_shortcuts.data.domains.app

import ch.rmy.android.framework.data.BaseRepository
import ch.rmy.android.framework.data.RealmFactory
import ch.rmy.android.framework.data.RealmTransactionContext
import ch.rmy.android.framework.extensions.deleteAllFromRealm
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.http_shortcuts.data.domains.getAppLock
import ch.rmy.android.http_shortcuts.data.domains.getBase
import ch.rmy.android.http_shortcuts.data.domains.getTemporaryShortcut
import ch.rmy.android.http_shortcuts.data.domains.getTemporaryVariable
import ch.rmy.android.http_shortcuts.data.models.AppLockModel
import ch.rmy.android.http_shortcuts.data.models.BaseModel
import ch.rmy.android.http_shortcuts.data.models.CategoryModel
import ch.rmy.android.http_shortcuts.data.models.HeaderModel
import ch.rmy.android.http_shortcuts.data.models.OptionModel
import ch.rmy.android.http_shortcuts.data.models.ParameterModel
import ch.rmy.android.http_shortcuts.data.models.PendingExecutionModel
import ch.rmy.android.http_shortcuts.data.models.ResolvedVariableModel
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.data.models.VariableModel
import ch.rmy.android.http_shortcuts.import_export.Importer
import io.realm.kotlin.where
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AppRepository
@Inject
constructor(
    realmFactory: RealmFactory,
) : BaseRepository(realmFactory) {

    suspend fun getBase(): BaseModel =
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

    suspend fun getLock(): AppLockModel? =
        query {
            getAppLock()
        }
            .firstOrNull()

    fun getObservableLock(): Flow<AppLockModel?> =
        observeQuery {
            getAppLock()
        }
            .map {
                it.firstOrNull()
            }

    suspend fun setLock(passwordHash: String) {
        commitTransaction {
            copyOrUpdate(AppLockModel(passwordHash))
        }
    }

    suspend fun removeLock() {
        commitTransaction {
            getAppLock()
                .findAll()
                .deleteAllFromRealm()
        }
    }

    suspend fun importBase(base: BaseModel, importMode: Importer.ImportMode) {
        commitTransaction {
            val oldBase = getBase().findFirst()!!
            if (base.title != null) {
                oldBase.title = base.title
            }
            if (!base.globalCode.isNullOrEmpty() && oldBase.globalCode.isNullOrEmpty()) {
                oldBase.globalCode = base.globalCode
            }
            when (importMode) {
                Importer.ImportMode.MERGE -> {
                    if (oldBase.categories.singleOrNull()?.shortcuts?.isEmpty() == true) {
                        oldBase.categories.clear()
                    }

                    base.categories.forEach { category ->
                        importCategory(oldBase, category)
                    }

                    val persistedVariables = copyOrUpdate(base.variables)
                    oldBase.variables.removeAll(persistedVariables.toSet())
                    oldBase.variables.addAll(persistedVariables)
                }
                Importer.ImportMode.REPLACE -> {
                    oldBase.categories.clear()
                    oldBase.categories.addAll(copyOrUpdate(base.categories))

                    oldBase.variables.clear()
                    oldBase.variables.addAll(copyOrUpdate(base.variables))
                }
            }
        }
    }

    private fun RealmTransactionContext.importCategory(base: BaseModel, category: CategoryModel) {
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

    private fun RealmTransactionContext.importShortcut(category: CategoryModel, shortcut: ShortcutModel) {
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
            realmInstance.where<CategoryModel>()
                .findAll()
                .filter {
                    it.id !in usedCategoryIds
                }
                .deleteAllFromRealm()

            // Delete orphaned shortcuts
            val usedShortcutIds = shortcuts.map { it.id }
            realmInstance.where<ShortcutModel>()
                .notEqualTo(ShortcutModel.FIELD_ID, ShortcutModel.TEMPORARY_ID)
                .findAll()
                .filter {
                    it.id !in usedShortcutIds
                }
                .deleteAllFromRealm()

            // Delete orphaned headers
            val usedHeaderIds = shortcuts
                .flatMap { it.headers }
                .map { header -> header.id }
            realmInstance.where<HeaderModel>()
                .findAll()
                .filter {
                    it.id !in usedHeaderIds
                }
                .deleteAllFromRealm()

            // Delete orphaned parameters
            val usedParameterIds = shortcuts
                .flatMap { it.parameters }
                .map { parameter -> parameter.id }
            realmInstance.where<ParameterModel>()
                .findAll()
                .filter {
                    it.id !in usedParameterIds
                }
                .deleteAllFromRealm()

            // Delete orphaned variables
            val usedVariableIds = variables.map { it.id }
            realmInstance.where<VariableModel>()
                .notEqualTo(VariableModel.FIELD_ID, VariableModel.TEMPORARY_ID)
                .findAll()
                .filter {
                    it.id !in usedVariableIds
                }
                .deleteAllFromRealm()

            // Delete orphaned options
            val usedOptionIds = variables.flatMap { it.options ?: emptyList() }.map { it.id }
            realmInstance.where<OptionModel>()
                .findAll()
                .filter {
                    it.id !in usedOptionIds
                }
                .deleteAllFromRealm()

            // Delete orphaned resolved variables
            val usedResolvedVariableIds = realmInstance.where<PendingExecutionModel>()
                .findAll()
                .flatMap { it.resolvedVariables }
                .map { it.id }
            realmInstance.where<ResolvedVariableModel>()
                .findAll()
                .filter {
                    it.id !in usedResolvedVariableIds
                }
                .deleteAllFromRealm()
        }
    }
}
