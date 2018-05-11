package ch.rmy.android.http_shortcuts.realm

import android.content.Context
import ch.rmy.android.http_shortcuts.BuildConfig
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.realm.models.Base
import ch.rmy.android.http_shortcuts.realm.models.Category
import ch.rmy.android.http_shortcuts.realm.models.HasId
import ch.rmy.android.http_shortcuts.realm.models.PendingExecution
import ch.rmy.android.http_shortcuts.realm.models.Shortcut
import ch.rmy.android.http_shortcuts.realm.models.Variable
import ch.rmy.android.http_shortcuts.utils.Destroyable
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.RealmResults
import org.jdeferred2.DoneFilter
import org.jdeferred2.Promise
import java.io.Closeable
import java.util.*

class Controller : Destroyable, Closeable {

    private val realm: Realm = realmFactory!!.createRealm()

    override fun destroy() {
        if (!realm.isClosed) {
            realm.close()
        }
    }

    override fun close() = destroy()

    fun getShortcuts() = Repository.getShortcuts(realm)

    fun getCategories(): RealmList<Category> = getBase().categories

    fun getVariables(): RealmList<Variable> = getBase().variables

    fun getCategoryById(id: Long) = Repository.getCategoryById(realm, id)

    fun getShortcutById(id: Long) = Repository.getShortcutById(realm, id)

    fun getShortcutByName(shortcutName: String): Shortcut? = Repository.getShortcutByName(realm, shortcutName)

    fun getShortcutsPendingExecution(): RealmResults<PendingExecution> = Repository.getShortcutsPendingExecution(realm)

    fun getShortcutPendingExecution(shortcutId: Long) = Repository.getShortcutPendingExecution(realm, shortcutId)

    fun getVariableById(id: Long) = Repository.getVariableById(realm, id)

    fun getVariableByKey(variableKey: String): Variable? = Repository.getVariableByKey(realm, variableKey)

    fun exportBase() = getBase().detachFromRealm()

    private fun getBase() = Repository.getBase(realm)!!

    fun setVariableValue(variableId: Long, value: String) =
            realm.commitAsync { realm ->
                Repository.getVariableById(realm, variableId)?.value = value
            }

    fun setVariableValue(variableKey: String, value: String) =
            realm.commitAsync { realm ->
                Repository.getVariableByKey(realm, variableKey)?.value = value
            }

    fun resetVariableValues(variableIds: List<Long>) =
            realm.commitAsync { realm ->
                variableIds.forEach { variableId ->
                    Repository.getVariableById(realm, variableId)?.value = ""
                }
            }

    fun importBaseSynchronously(base: Base) {
        val oldBase = getBase()
        realm.executeTransaction { realm ->
            val persistedCategories = realm.copyToRealmOrUpdate(base.categories)
            oldBase.categories.removeAll(persistedCategories)
            oldBase.categories.addAll(persistedCategories)

            val persistedVariables = realm.copyToRealmOrUpdate(base.variables)
            oldBase.variables.removeAll(persistedVariables)
            oldBase.variables.addAll(persistedVariables)
        }
    }

    fun deleteShortcut(shortcutId: Long) =
            realm.commitAsync { realm ->
                val shortcut = Repository.getShortcutById(realm, shortcutId) ?: return@commitAsync
                Repository.getShortcutPendingExecution(realm, shortcutId)?.deleteFromRealm()
                shortcut.headers.deleteAllFromRealm()
                shortcut.parameters.deleteAllFromRealm()
                shortcut.deleteFromRealm()
            }

    fun moveShortcut(shortcutId: Long, targetPosition: Int? = null, targetCategoryId: Long? = null) =
            realm.commitAsync { realm ->
                val shortcut = Repository.getShortcutById(realm, shortcutId) ?: return@commitAsync
                val categories = Repository.getBase(realm)?.categories ?: return@commitAsync
                val targetCategory = if (targetCategoryId != null) {
                    Repository.getCategoryById(realm, targetCategoryId)
                } else {
                    categories.first { it.shortcuts.any { it.id == shortcutId } }
                } ?: return@commitAsync

                for (category in categories) {
                    category.shortcuts.remove(shortcut)
                }
                if (targetPosition != null) {
                    targetCategory.shortcuts.add(targetPosition, shortcut)
                } else {
                    targetCategory.shortcuts.add(shortcut)
                }
            }

    fun renameShortcut(shortcutId: Long, newName: String) =
            realm.commitAsync { realm ->
                Repository.getShortcutById(realm, shortcutId)?.name = newName
            }

    fun createCategory(name: String) =
            realm.commitAsync { realm ->
                val base = Repository.getBase(realm) ?: return@commitAsync
                val categories = base.categories
                val category = Category.createNew(name)
                category.id = generateId(realm, Category::class.java)
                categories.add(realm.copyToRealm(category))
            }

    fun renameCategory(categoryId: Long, newName: String) =
            realm.commitAsync { realm ->
                Repository.getCategoryById(realm, categoryId)?.name = newName
            }

    fun setLayoutType(categoryId: Long, layoutType: String) =
            realm.commitAsync { realm ->
                Repository.getCategoryById(realm, categoryId)?.layoutType = layoutType
            }

    fun moveCategory(categoryId: Long, position: Int) =
            realm.commitAsync { realm ->
                val base = Repository.getBase(realm) ?: return@commitAsync
                val category = Repository.getCategoryById(realm, categoryId) ?: return@commitAsync
                val categories = base.categories
                val oldPosition = categories.indexOf(category)
                categories.move(oldPosition, position)
            }

    fun deleteCategory(categoryId: Long) =
            realm.commitAsync { realm ->
                val category = Repository.getCategoryById(realm, categoryId) ?: return@commitAsync
                for (shortcut in category.shortcuts) {
                    shortcut.headers.deleteAllFromRealm()
                    shortcut.parameters.deleteAllFromRealm()
                }
                category.shortcuts.deleteAllFromRealm()
                category.deleteFromRealm()
            }

    fun moveVariable(variableId: Long, position: Int) =
            realm.commitAsync { realm ->
                val base = Repository.getBase(realm) ?: return@commitAsync
                val variable = Repository.getVariableById(realm, variableId) ?: return@commitAsync
                val variables = base.variables
                val oldPosition = variables.indexOf(variable)
                variables.move(oldPosition, position)
            }

    fun deleteVariable(variableId: Long) =
            realm.commitAsync { realm ->
                val variable = Repository.getVariableById(realm, variableId) ?: return@commitAsync
                variable.options?.deleteAllFromRealm()
                variable.deleteFromRealm()
            }

    fun createPendingExecution(
            shortcutId: Long,
            resolvedVariables: Map<String, String>,
            tryNumber: Int = 0,
            waitUntil: Date? = null,
            requiresNetwork: Boolean
    ) =
            realm.commitAsync { realm ->
                val alreadyPending = Repository.getShortcutPendingExecution(realm, shortcutId) != null
                if (!alreadyPending) {
                    realm.copyToRealm(PendingExecution.createNew(shortcutId, resolvedVariables, tryNumber, waitUntil, requiresNetwork))
                }
            }

    fun removePendingExecution(shortcutId: Long) =
            realm.commitAsync { realm ->
                Repository.getShortcutPendingExecution(realm, shortcutId)?.deleteFromRealm()
            }

    fun persist(shortcut: Shortcut): Promise<Shortcut, Throwable, Unit> {
        if (shortcut.isNew) {
            shortcut.id = generateId(realm, Shortcut::class.java)
        }
        return realm.commitAsync { realm ->
            realm.copyToRealmOrUpdate(shortcut)
        }
                .then(DoneFilter { getShortcutById(shortcut.id)!! })
    }

    fun persist(variable: Variable): Promise<Variable, Throwable, Unit> {
        val isNew = variable.isNew
        if (isNew) {
            variable.id = generateId(realm, Variable::class.java)
        }
        return realm.commitAsync { realm ->
            val base = Repository.getBase(realm) ?: return@commitAsync
            val newVariable = realm.copyToRealmOrUpdate(variable)
            if (isNew) {
                base.variables.add(newVariable)
            }
        }
                .then(DoneFilter { getVariableById(variable.id)!! })
    }

    private fun generateId(realm: Realm, clazz: Class<out RealmObject>): Long {
        val maxId = realm.where(clazz).max(HasId.FIELD_ID)
        val maxIdLong = Math.max(maxId?.toLong() ?: 0, 0)
        return maxIdLong + 1
    }

    companion object {

        private var realmFactory: RealmFactory? = null

        fun init(context: Context) {
            if (realmFactory != null) {
                return
            }

            Realm.init(context)
            realmFactory = RealmFactory(BuildConfig.REALM_ENCRYPTION_KEY.toByteArray())
            realmFactory!!.createRealm().use { realm ->
                if (Repository.getBase(realm) == null) {
                    setupBase(context, realm)
                }
            }
        }

        private fun setupBase(context: Context, realm: Realm) {
            val defaultCategoryName = context.getString(R.string.shortcuts)
            realm.executeTransaction {
                val defaultCategory = Category.createNew(defaultCategoryName)
                defaultCategory.id = 1

                val newBase = Base()
                newBase.categories = RealmList()
                newBase.variables = RealmList()
                newBase.categories.add(defaultCategory)
                it.copyToRealm(newBase)
            }
        }

    }

}
