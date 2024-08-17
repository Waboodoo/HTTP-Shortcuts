package ch.rmy.android.http_shortcuts.data.domains.shortcuts

import ch.rmy.android.framework.data.BaseRepository
import ch.rmy.android.framework.data.RealmFactory
import ch.rmy.android.framework.data.RealmTransactionContext
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.getBase
import ch.rmy.android.http_shortcuts.data.domains.getCategoryById
import ch.rmy.android.http_shortcuts.data.domains.getShortcutById
import ch.rmy.android.http_shortcuts.data.domains.getShortcutByNameOrId
import ch.rmy.android.http_shortcuts.data.domains.getTemporaryShortcut
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.models.ResponseHandling
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import io.realm.kotlin.ext.copyFromRealm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ShortcutRepository
@Inject
constructor(
    realmFactory: RealmFactory,
) : BaseRepository(realmFactory) {

    suspend fun getShortcutById(shortcutId: ShortcutId): Shortcut =
        query {
            getShortcutById(shortcutId)
        }
            .first()

    suspend fun getShortcutsByIds(shortcutIds: Collection<ShortcutId>): List<Shortcut> =
        queryItem {
            getBase()
        }
            .shortcuts.filter { it.id in shortcutIds }

    suspend fun getShortcutByNameOrId(shortcutNameOrId: ShortcutNameOrId): Shortcut =
        queryItem {
            getShortcutByNameOrId(shortcutNameOrId)
        }

    fun getObservableShortcuts(): Flow<List<Shortcut>> =
        observeList {
            getBase().findFirst()!!.categories
        }
            .map { categories ->
                categories.flatMap { category ->
                    category.shortcuts
                }
            }

    suspend fun getShortcuts(): List<Shortcut> =
        queryItem {
            getBase()
        }
            .shortcuts

    suspend fun moveShortcuts(placement: Map<CategoryId, List<ShortcutId>>) {
        commitTransaction {
            val base = getBase().findFirst() ?: return@commitTransaction
            val shortcutMap = base.shortcuts.associateBy { it.id }
            val categories = base.categories

            // Some sanity checking first
            assert(categories.map { it.id }.toSet() == placement.keys) {
                "Category IDs in placement did not match existing categories"
            }
            assert(placement.values.flatten().toSet() == base.shortcuts.map { it.id }.toSet()) {
                "Shortcut IDs in placement did not match existing shortcuts"
            }

            categories.forEach { category ->
                category.shortcuts.apply {
                    clear()
                    addAll(placement[category.id]!!.map { shortcutId -> shortcutMap[shortcutId]!! })
                }
            }
        }
    }

    private fun RealmTransactionContext.moveShortcut(shortcutId: ShortcutId, targetPosition: Int? = null, targetCategoryId: CategoryId? = null) {
        val shortcut = getShortcutById(shortcutId)
            .findFirst() ?: return
        val categories = getBase()
            .findFirst()
            ?.categories ?: return
        val targetCategory = if (targetCategoryId != null) {
            getCategoryById(targetCategoryId)
                .findFirst()
        } else {
            categories.first { category -> category.shortcuts.any { it.id == shortcutId } }
        } ?: return

        for (category in categories) {
            category.shortcuts.remove(shortcut)
        }
        if (targetPosition != null) {
            targetCategory.shortcuts.add(targetPosition, shortcut)
        } else {
            targetCategory.shortcuts.add(shortcut)
        }
    }

    suspend fun duplicateShortcut(shortcutId: ShortcutId, newName: String, newPosition: Int?, categoryId: CategoryId) {
        commitTransaction {
            val shortcut = getShortcutById(shortcutId)
                .findFirst()
                ?: return@commitTransaction
            val newShortcut = copyShortcut(shortcut, newUUID())
            newShortcut.name = newName
            moveShortcut(newShortcut.id, newPosition, categoryId)
        }
    }

    suspend fun createTemporaryShortcutFromShortcut(shortcutId: ShortcutId, categoryId: CategoryId) {
        commitTransaction {
            val shortcut = getShortcutById(shortcutId)
                .findFirst()!!
            copyShortcut(shortcut, Shortcut.TEMPORARY_ID)
                .apply {
                    this.categoryId = categoryId
                }
        }
    }

    suspend fun copyTemporaryShortcutToShortcut(shortcutId: ShortcutId, categoryId: CategoryId?) {
        commitTransaction {
            val temporaryShortcut = getTemporaryShortcut()
                .findFirst() ?: return@commitTransaction
            val shortcut = copyShortcut(temporaryShortcut, shortcutId)
            if (categoryId != null) {
                val category = getCategoryById(categoryId)
                    .findFirst()
                    ?: return@commitTransaction
                if (category.shortcuts.none { it.id == shortcutId }) {
                    category.shortcuts.add(shortcut)
                }
            }
        }
    }

    private fun RealmTransactionContext.copyShortcut(sourceShortcut: Shortcut, targetShortcutId: ShortcutId): Shortcut =
        sourceShortcut.copyFromRealm()
            .apply {
                id = targetShortcutId
                parameters.forEach { parameter ->
                    parameter.id = newUUID()
                }
                headers.forEach { header ->
                    header.id = newUUID()
                }
                if (executionType == ShortcutExecutionType.APP.type && responseHandling == null) {
                    responseHandling = ResponseHandling()
                }
            }
            .let(::copyOrUpdate)

    suspend fun deleteShortcut(shortcutId: ShortcutId) {
        commitTransaction {
            getShortcutById(shortcutId)
                .findFirst()
                ?.apply {
                    headers.deleteAll()
                    parameters.deleteAll()
                    responseHandling?.delete()
                    delete()
                }
        }
    }

    suspend fun setIcon(shortcutId: ShortcutId, icon: ShortcutIcon) {
        commitTransaction {
            getShortcutById(shortcutId)
                .findFirst()
                ?.icon = icon
        }
    }

    suspend fun setName(shortcutId: ShortcutId, name: String) {
        commitTransaction {
            getShortcutById(shortcutId)
                .findFirst()
                ?.name = name
        }
    }

    suspend fun setDescription(shortcutId: ShortcutId, description: String) {
        commitTransaction {
            getShortcutById(shortcutId)
                .findFirst()
                ?.description = description
        }
    }

    suspend fun setHidden(shortcutId: ShortcutId, hidden: Boolean) {
        commitTransaction {
            getShortcutById(shortcutId)
                .findFirst()
                ?.hidden = hidden
        }
    }
}
