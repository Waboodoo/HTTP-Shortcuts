package ch.rmy.android.http_shortcuts.data.domains.app

import ch.rmy.android.framework.data.BaseRepository
import ch.rmy.android.framework.data.RealmTransactionContext
import ch.rmy.android.framework.extensions.mapIfNotNull
import ch.rmy.android.framework.utils.Optional
import ch.rmy.android.http_shortcuts.data.RealmFactory
import ch.rmy.android.http_shortcuts.data.domains.getAppLock
import ch.rmy.android.http_shortcuts.data.domains.getBase
import ch.rmy.android.http_shortcuts.data.domains.getTemporaryShortcut
import ch.rmy.android.http_shortcuts.data.domains.getTemporaryVariable
import ch.rmy.android.http_shortcuts.data.models.AppLock
import ch.rmy.android.http_shortcuts.data.models.Base
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.Header
import ch.rmy.android.http_shortcuts.data.models.Option
import ch.rmy.android.http_shortcuts.data.models.Parameter
import ch.rmy.android.http_shortcuts.data.models.PendingExecution
import ch.rmy.android.http_shortcuts.data.models.ResolvedVariable
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.import_export.Importer
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.realm.RealmObject
import io.realm.kotlin.where

class AppRepository : BaseRepository(RealmFactory.getInstance()) {

    fun getBase(): Single<Base> =
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

    fun getLock(): Single<Optional<AppLock>> =
        query {
            getAppLock()
        }
            .map {
                Optional(it.firstOrNull())
            }

    fun getObservableLock(): Observable<Optional<AppLock>> =
        observeQuery {
            getAppLock()
        }
            .map {
                Optional(it.firstOrNull())
            }

    fun setLock(passwordHash: String): Completable =
        commitTransaction {
            copyOrUpdate(AppLock(passwordHash))
        }

    fun removeLock(): Completable =
        commitTransaction {
            getAppLock()
                .findAll()
                .deleteAllFromRealm()
        }

    fun importBase(base: Base, importMode: Importer.ImportMode) =
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

    private fun importCategory(realmTransactionContext: RealmTransactionContext, base: Base, category: Category) {
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

    private fun importShortcut(realmTransactionContext: RealmTransactionContext, category: Category, shortcut: Shortcut) {
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
                .mapIfNotNull(temporaryShortcut) {
                    plus(it)
                }
            val variables = base.variables.toList()
                .mapIfNotNull(temporaryVariable) {
                    plus(it)
                }

            // Delete orphaned categories
            val usedCategoryIds = categories.map { it.id }
            realmInstance.where<Category>()
                .findAll()
                .filter {
                    it.id !in usedCategoryIds
                }
                .deleteAllFromRealm()

            // Delete orphaned shortcuts
            val usedShortcutIds = shortcuts.map { it.id }
            realmInstance.where<Shortcut>()
                .notEqualTo(Shortcut.FIELD_ID, Shortcut.TEMPORARY_ID)
                .findAll()
                .filter {
                    it.id !in usedShortcutIds
                }
                .deleteAllFromRealm()

            // Delete orphaned headers
            val usedHeaderIds = shortcuts
                .flatMap { it.headers }
                .map { header -> header.id }
            realmInstance.where<Header>()
                .findAll()
                .filter {
                    it.id !in usedHeaderIds
                }
                .deleteAllFromRealm()

            // Delete orphaned parameters
            val usedParameterIds = shortcuts
                .flatMap { it.parameters }
                .map { parameter -> parameter.id }
            realmInstance.where<Parameter>()
                .findAll()
                .filter {
                    it.id !in usedParameterIds
                }
                .deleteAllFromRealm()

            // Delete orphaned variables
            val usedVariableIds = variables.map { it.id }
            realmInstance.where<Variable>()
                .notEqualTo(Variable.FIELD_ID, Variable.TEMPORARY_ID)
                .findAll()
                .filter {
                    it.id !in usedVariableIds
                }
                .deleteAllFromRealm()

            // Delete orphaned options
            val usedOptionIds = variables.flatMap { it.options ?: emptyList() }.map { it.id }
            realmInstance.where<Option>()
                .findAll()
                .filter {
                    it.id !in usedOptionIds
                }
                .deleteAllFromRealm()

            // Delete orphaned resolved variables
            val usedResolvedVariableIds = realmInstance.where<PendingExecution>()
                .findAll()
                .flatMap { it.resolvedVariables }
                .map { it.id }
            realmInstance.where<ResolvedVariable>()
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
