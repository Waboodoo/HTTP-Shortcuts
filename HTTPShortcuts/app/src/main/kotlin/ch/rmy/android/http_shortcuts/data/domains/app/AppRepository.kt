package ch.rmy.android.http_shortcuts.data.domains.app

import ch.rmy.android.framework.data.BaseRepository
import ch.rmy.android.framework.data.RealmFactory
import ch.rmy.android.framework.data.RealmTransactionContext
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.utils.Optional
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
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.kotlin.where
import javax.inject.Inject

class AppRepository
@Inject
constructor(
    realmFactory: RealmFactory,
) : BaseRepository(realmFactory) {

    fun getBase(): Single<BaseModel> =
        queryItem {
            getBase()
        }

    fun getGlobalCode(): Single<String> =
        query {
            getBase()
        }
            .map {
                it.firstOrNull()
                    ?.globalCode
                    ?: ""
            }

    fun getPollingShortcuts(): Single<RealmList<ShortcutModel>> =
        query {
            getBase()
        }
            .map {
                it.first().let { base ->
                    base.pollingShortcuts
                }
            }

    fun setPollingShortcuts(pollingShortcuts: List<ShortcutModel>): Completable =
        commitTransaction {
            getBase()
                .findFirst()
                ?.let { base ->
                    base.pollingShortcuts.clear()
                    base.pollingShortcuts.addAll(pollingShortcuts)
                }
        }

    fun getToolbarTitle(): Single<String> =
        queryItem {
            getBase()
        }
            .map { base ->
                base.title?.takeUnless { it.isBlank() } ?: ""
            }

    fun getObservableToolbarTitle(): Observable<String> =
        observeItem {
            getBase()
        }
            .map { base ->
                base.title?.takeUnless { it.isBlank() } ?: ""
            }

    fun setToolbarTitle(title: String): Completable =
        commitTransaction {
            getBase()
                .findFirst()
                ?.title = title
        }

    fun setGlobalCode(globalCode: String?): Completable =
        commitTransaction {
            getBase()
                .findFirst()
                ?.let { base ->
                    base.globalCode = globalCode
                }
        }

    fun getLock(): Single<Optional<AppLockModel>> =
        query {
            getAppLock()
        }
            .map {
                Optional(it.firstOrNull())
            }

    fun getObservableLock(): Observable<Optional<AppLockModel>> =
        observeQuery {
            getAppLock()
        }
            .map {
                Optional(it.firstOrNull())
            }

    fun setLock(passwordHash: String): Completable =
        commitTransaction {
            copyOrUpdate(AppLockModel(passwordHash))
        }

    fun removeLock(): Completable =
        commitTransaction {
            getAppLock()
                .findAll()
                .deleteAllFromRealm()
        }

    fun importBase(base: BaseModel, importMode: Importer.ImportMode) =
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
                        importCategory(this, oldBase, category)
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

    private fun importCategory(realmTransactionContext: RealmTransactionContext, base: BaseModel, category: CategoryModel) {
        val oldCategory = base.categories.find { it.id == category.id }
        if (oldCategory == null) {
            base.categories.add(realmTransactionContext.copyOrUpdate(category))
        } else {
            oldCategory.name = category.name
            oldCategory.categoryBackgroundType = category.categoryBackgroundType
            oldCategory.hidden = category.hidden
            oldCategory.categoryLayoutType = category.categoryLayoutType
            category.shortcuts.forEach { shortcut ->
                importShortcut(realmTransactionContext, oldCategory, shortcut)
            }
        }
    }

    private fun importShortcut(realmTransactionContext: RealmTransactionContext, category: CategoryModel, shortcut: ShortcutModel) {
        val oldShortcut = category.shortcuts.find { it.id == shortcut.id }
        if (oldShortcut == null) {
            category.shortcuts.add(realmTransactionContext.copyOrUpdate(shortcut))
        } else {
            realmTransactionContext.copyOrUpdate(shortcut)
        }
    }

    fun deleteUnusedData() =
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

    private fun <T : RealmObject> List<T>.deleteAllFromRealm() {
        forEach {
            it.deleteFromRealm()
        }
    }
}
