package ch.rmy.android.http_shortcuts.data.domains.app

import ch.rmy.android.framework.data.BaseRealmRepository
import ch.rmy.android.framework.data.RealmFactory
import ch.rmy.android.framework.data.RealmTransactionContext
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.domains.getBase
import ch.rmy.android.http_shortcuts.data.domains.getTemporaryShortcut
import ch.rmy.android.http_shortcuts.data.models.Base
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.Header
import ch.rmy.android.http_shortcuts.data.models.Parameter
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.import_export.ImportExportBase
import ch.rmy.android.http_shortcuts.import_export.Importer
import javax.inject.Inject

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

    suspend fun import(base: ImportExportBase, mode: Importer.ImportMode) {
        commitTransaction {
            logInfo("Importing base ($mode)")
            val oldBase = getBase().findFirst()!!
            when (mode) {
                Importer.ImportMode.MERGE -> {
                    if (oldBase.categories.singleOrNull()?.shortcuts?.isEmpty() == true) {
                        oldBase.categories.singleOrNull()?.delete()
                        oldBase.categories.clear()
                    }

                    base.categories.forEach { category ->
                        importCategory(oldBase, category)
                    }
                }
                Importer.ImportMode.REPLACE -> {
                    oldBase.categories.clear()
                    oldBase.categories.addAll(copyOrUpdate(base.categories))
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
            val categories = base.categories
            val shortcuts = base.shortcuts
                .runIfNotNull(temporaryShortcut) {
                    plus(it)
                }

            // Delete orphaned categories
            val usedCategoryIds = categories.map { it.id }
            get<Category>()
                .find()
                .filter {
                    it.id !in usedCategoryIds
                }
                .deleteAll()

            // Delete orphaned shortcuts
            val usedShortcutIds = shortcuts.map { it.id }
            get<Shortcut>("${Shortcut.FIELD_ID} != $0", Shortcut.TEMPORARY_ID)
                .find()
                .filter {
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
                    it.id !in usedParameterIds
                }
                .deleteAll()
        }
    }
}
