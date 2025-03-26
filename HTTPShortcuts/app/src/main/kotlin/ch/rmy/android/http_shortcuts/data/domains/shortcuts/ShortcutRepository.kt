package ch.rmy.android.http_shortcuts.data.domains.shortcuts

import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.domains.BaseRepository
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.sections.SectionId
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.Shortcut.Companion.TEMPORARY_ID
import ch.rmy.android.http_shortcuts.extensions.ids
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import javax.inject.Inject
import kotlin.collections.forEach
import kotlin.collections.map
import kotlinx.coroutines.flow.Flow

class ShortcutRepository
@Inject
constructor(
    database: Database,
) : BaseRepository(database) {

    suspend fun getShortcutById(shortcutId: ShortcutId): Shortcut = query {
        shortcutDao().getShortcutById(shortcutId).first()
    }

    suspend fun getShortcutsByIds(shortcutIds: Collection<ShortcutId>): List<Shortcut> = query {
        shortcutDao().getShortcutsByIds(shortcutIds)
    }

    suspend fun getShortcutByNameOrId(shortcutNameOrId: ShortcutNameOrId): Shortcut = query {
        shortcutDao().getShortcutByNameOrId(shortcutNameOrId).first()
    }

    fun observeShortcuts(): Flow<List<Shortcut>> = queryFlow {
        shortcutDao().observeShortcuts()
    }

    fun observeShortcutsByCategoryId(categoryId: CategoryId): Flow<List<Shortcut>> = queryFlow {
        shortcutDao().observeShortcutsByCategoryId(categoryId)
    }

    suspend fun getShortcuts(): List<Shortcut> = query {
        shortcutDao().getShortcuts()
    }

    suspend fun getQuickSettingsShortcuts(): List<Shortcut> = query {
        shortcutDao().getQuickSettingsShortcuts()
    }

    suspend fun hasSecondaryLauncherShortcuts(): Boolean = query {
        shortcutDao().countSecondaryLauncherShortcuts() != 0
    }

    suspend fun moveShortcuts(placement: Map<Pair<CategoryId, SectionId?>, List<ShortcutId>>) {
        commitTransaction {
            val shortcutDao = shortcutDao()
            val categories = categoryDao().getCategories()
            val sectionsByCategoryId = sectionDao().getSections().groupBy { it.categoryId }

            // Some sanity checking first
            assert(categories.ids().toSet() == placement.keys.map { it.first }.toSet()) {
                "Category IDs in placement did not match existing categories"
            }
            assert(placement.values.flatten().toSet() == shortcutDao.getShortcuts().ids().toSet()) {
                "Shortcut IDs in placement did not match existing shortcuts"
            }

            categories.forEach { category ->
                var sortingOrder = 0
                (listOf(null) + (sectionsByCategoryId[category.id]?.ids() ?: emptyList())).forEach { sectionId ->
                    placement[category.id to sectionId]?.forEach { shortcutId ->
                        shortcutDao.moveShortcut(
                            shortcutId = shortcutId,
                            categoryId = category.id,
                            sectionId = sectionId,
                            sortingOrder = sortingOrder,
                        )
                        sortingOrder++
                    }
                }
            }
        }
    }

    suspend fun duplicateShortcut(shortcutId: ShortcutId, newName: String) {
        commitTransaction {
            val shortcutDao = shortcutDao()
            val shortcut = shortcutDao.getShortcutById(shortcutId)
                .firstOrNull()
                ?: return@commitTransaction
            val newShortcut = shortcut.copy(
                id = newUUID(),
                name = newName,
                sortingOrder = shortcut.sortingOrder + 1,
            )
            shortcutDao.updateSortingOrder(
                categoryId = shortcut.categoryId,
                from = shortcut.sortingOrder + 1,
                until = Int.MAX_VALUE,
                diff = 1,
            )
            shortcutDao.insertOrUpdateShortcut(newShortcut)
            copyRequestHeaders(sourceShortcutId = shortcutId, targetShortcutId = newShortcut.id)
            copyRequestParameters(sourceShortcutId = shortcutId, targetShortcutId = newShortcut.id)
        }
    }

    private suspend fun Database.copyRequestHeaders(sourceShortcutId: ShortcutId, targetShortcutId: ShortcutId) {
        val requestHeaderDao = requestHeaderDao()
        requestHeaderDao.getRequestHeadersByShortcutId(sourceShortcutId)
            .map { header ->
                header.copy(
                    id = 0,
                    shortcutId = targetShortcutId,
                )
            }
            .forEach { header ->
                requestHeaderDao.insertOrUpdateRequestHeader(header)
            }
    }

    private suspend fun Database.copyRequestParameters(sourceShortcutId: ShortcutId, targetShortcutId: ShortcutId) {
        val requestParameterDao = requestParameterDao()
        requestParameterDao.getRequestParametersByShortcutId(sourceShortcutId)
            .map { parameter ->
                parameter.copy(
                    id = 0,
                    shortcutId = targetShortcutId,
                )
            }
            .forEach { parameter ->
                requestParameterDao.insertOrUpdateRequestParameter(parameter)
            }
    }

    suspend fun createTemporaryShortcutFromShortcut(shortcutId: ShortcutId) {
        commitTransaction {
            val shortcutDao = shortcutDao()
            val shortcut = shortcutDao.getShortcutById(shortcutId)
                .firstOrNull()
                ?: return@commitTransaction
            shortcutDao.insertOrUpdateShortcut(
                shortcut.copy(
                    id = TEMPORARY_ID,
                ),
            )
            requestHeaderDao().deleteRequestHeaderByShortcutId(TEMPORARY_ID)
            copyRequestHeaders(sourceShortcutId = shortcutId, targetShortcutId = TEMPORARY_ID)
            requestParameterDao().deleteRequestParametersByShortcutId(TEMPORARY_ID)
            copyRequestParameters(sourceShortcutId = shortcutId, targetShortcutId = TEMPORARY_ID)
        }
    }

    suspend fun copyTemporaryShortcutToShortcut(shortcutId: ShortcutId) {
        commitTransaction {
            val shortcutDao = shortcutDao()
            val temporaryShortcut = shortcutDao.getShortcutById(TEMPORARY_ID)
                .firstOrNull()
                ?: return@commitTransaction
            val oldShortcut = shortcutDao.getShortcutById(shortcutId)
                .firstOrNull()

            val newShortcut = if (oldShortcut != null) {
                // If the old shortcut exists, i.e., we were editing an existing shortcut, we need to preserve its position,
                // as there is the (slim) chance that it was moved in between the creation of the temporary shortcut and now.
                temporaryShortcut.copy(
                    id = shortcutId,
                    categoryId = oldShortcut.categoryId,
                    sectionId = oldShortcut.sectionId,
                    sortingOrder = oldShortcut.sortingOrder,
                )
            } else {
                // If the old shortcut does not exist, i.e., we were editing a new not-yet-persisted shortcut, we need to determine
                // its position within the category
                temporaryShortcut.copy(
                    id = shortcutId,
                    sortingOrder = shortcutDao.getShortcutCountByCategoryId(temporaryShortcut.categoryId),
                )
            }
            shortcutDao.insertOrUpdateShortcut(newShortcut)

            if (oldShortcut != null) {
                requestHeaderDao().deleteRequestHeaderByShortcutId(shortcutId)
                requestParameterDao().deleteRequestParametersByShortcutId(shortcutId)
            }
            copyRequestHeaders(sourceShortcutId = TEMPORARY_ID, targetShortcutId = shortcutId)
            if (newShortcut.usesRequestParameters()) {
                copyRequestParameters(sourceShortcutId = TEMPORARY_ID, targetShortcutId = shortcutId)
            }

            shortcutDao.deleteShortcutById(TEMPORARY_ID)
            requestHeaderDao().deleteRequestHeaderByShortcutId(TEMPORARY_ID)
            requestParameterDao().deleteRequestParametersByShortcutId(TEMPORARY_ID)
        }
    }

    suspend fun deleteShortcut(shortcutId: ShortcutId) {
        commitTransaction {
            shortcutDao().deleteShortcutById(shortcutId)
            requestHeaderDao().deleteRequestHeaderByShortcutId(shortcutId)
            requestParameterDao().deleteRequestParametersByShortcutId(shortcutId)
        }
    }

    suspend fun setIcon(shortcutId: ShortcutId, icon: ShortcutIcon) {
        commitTransactionForShortcut(shortcutId) { shortcut ->
            shortcutDao().insertOrUpdateShortcut(
                shortcut.copy(icon = icon),
            )
        }
    }

    suspend fun setName(shortcutId: ShortcutId, name: String) {
        commitTransactionForShortcut(shortcutId) { shortcut ->
            shortcutDao().insertOrUpdateShortcut(
                shortcut.copy(name = name),
            )
        }
    }

    suspend fun setDescription(shortcutId: ShortcutId, description: String) {
        commitTransactionForShortcut(shortcutId) { shortcut ->
            shortcutDao().insertOrUpdateShortcut(
                shortcut.copy(description = description),
            )
        }
    }

    suspend fun setHidden(shortcutId: ShortcutId, hidden: Boolean) {
        commitTransactionForShortcut(shortcutId) { shortcut ->
            shortcutDao().insertOrUpdateShortcut(
                shortcut.copy(hidden = hidden),
            )
        }
    }

    private suspend fun commitTransactionForShortcut(shortcutId: ShortcutId, transaction: suspend Database.(Shortcut) -> Unit) {
        commitTransaction {
            transaction(
                shortcutDao().getShortcutById(shortcutId)
                    .firstOrNull()
                    ?: return@commitTransaction,
            )
        }
    }
}
