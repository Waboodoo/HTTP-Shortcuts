package ch.rmy.android.http_shortcuts.data.domains.shortcuts

import ch.rmy.android.framework.data.BaseRepository
import ch.rmy.android.framework.data.RealmFactory
import ch.rmy.android.framework.data.RealmTransactionContext
import ch.rmy.android.framework.extensions.detachFromRealm
import ch.rmy.android.framework.extensions.swap
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.getBase
import ch.rmy.android.http_shortcuts.data.domains.getCategoryById
import ch.rmy.android.http_shortcuts.data.domains.getShortcutById
import ch.rmy.android.http_shortcuts.data.domains.getShortcutByNameOrId
import ch.rmy.android.http_shortcuts.data.domains.getTemporaryShortcut
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import io.realm.kotlin.deleteFromRealm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ShortcutRepository
@Inject
constructor(
    realmFactory: RealmFactory,
) : BaseRepository(realmFactory) {

    suspend fun getShortcutById(shortcutId: ShortcutId): ShortcutModel =
        query {
            getShortcutById(shortcutId)
        }
            .first()

    suspend fun getShortcutsByIds(shortcutIds: Collection<ShortcutId>): List<ShortcutModel> =
        queryItem {
            getBase()
        }
            .shortcuts.filter { it.id in shortcutIds }

    suspend fun getShortcutByNameOrId(shortcutNameOrId: ShortcutNameOrId): ShortcutModel =
        queryItem {
            getShortcutByNameOrId(shortcutNameOrId)
        }

    fun getObservableShortcuts(): Flow<List<ShortcutModel>> =
        observeList {
            getBase().findFirst()!!.categories
        }
            .map { categories ->
                categories.flatMap { category ->
                    category.shortcuts
                }
            }

    suspend fun getShortcuts(): List<ShortcutModel> =
        queryItem {
            getBase()
        }
            .shortcuts

    suspend fun moveShortcutToCategory(shortcutId: ShortcutId, targetCategoryId: CategoryId) {
        commitTransaction {
            moveShortcut(shortcutId, targetCategoryId = targetCategoryId)
        }
    }

    suspend fun swapShortcutPositions(categoryId: CategoryId, shortcutId1: ShortcutId, shortcutId2: ShortcutId) {
        commitTransaction {
            getCategoryById(categoryId)
                .findFirst()
                ?.shortcuts
                ?.swap(shortcutId1, shortcutId2) { id }
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

    suspend fun createTemporaryShortcutFromShortcut(shortcutId: ShortcutId) {
        commitTransaction {
            val shortcut = getShortcutById(shortcutId)
                .findFirst()!!
            copyShortcut(shortcut, ShortcutModel.TEMPORARY_ID)
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

    private fun RealmTransactionContext.copyShortcut(sourceShortcut: ShortcutModel, targetShortcutId: ShortcutId): ShortcutModel =
        sourceShortcut.detachFromRealm()
            .apply {
                id = targetShortcutId
                parameters.forEach { parameter ->
                    parameter.id = newUUID()
                }
                headers.forEach { header ->
                    header.id = newUUID()
                }
            }
            .let(::copyOrUpdate)

    suspend fun deleteShortcut(shortcutId: ShortcutId) {
        commitTransaction {
            getShortcutById(shortcutId)
                .findFirst()
                ?.apply {
                    headers.deleteAllFromRealm()
                    parameters.deleteAllFromRealm()
                    responseHandling?.deleteFromRealm()
                    deleteFromRealm()
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
}
