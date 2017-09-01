package ch.rmy.android.http_shortcuts.realm

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.realm.models.*
import ch.rmy.android.http_shortcuts.utils.Destroyable
import io.realm.*
import java.util.*

class Controller : Destroyable {

    private val realm: Realm = RealmFactory.realm

    override fun destroy() {
        realm.close()
    }

    fun getCategoryById(id: Long) = realm.where(Category::class.java).equalTo(FIELD_ID, id).findFirst()

    fun getShortcutById(id: Long) = realm.where(Shortcut::class.java).equalTo(FIELD_ID, id).findFirst()

    fun getShortcutByName(shortcutName: String) = realm.where(Shortcut::class.java).equalTo(Shortcut.FIELD_NAME, shortcutName, Case.INSENSITIVE).findFirst()

    fun getVariableById(id: Long) = realm.where(Variable::class.java).equalTo(FIELD_ID, id).findFirst()

    fun getVariableByKey(key: String) = realm.where(Variable::class.java).equalTo(Variable.FIELD_KEY, key).findFirst()

    fun setVariableValue(variable: Variable, value: String) {
        realm.executeTransaction { variable.value = value }
    }

    fun getDetachedShortcutById(id: Long): Shortcut? {
        val shortcut = getShortcutById(id) ?: return null
        return realm.copyFromRealm(shortcut)
    }

    fun getDetachedVariableById(id: Long): Variable? {
        val variable = getVariableById(id) ?: return null
        return realm.copyFromRealm(variable)
    }

    val base: Base
        get() = realm.where(Base::class.java).findFirst()

    fun exportBase(): Base {
        return realm.copyFromRealm(base)
    }

    fun importBase(base: Base) {
        val oldBase = base
        realm.executeTransaction { realm ->
            val persistedCategories = realm.copyToRealmOrUpdate(base.categories)
            oldBase.categories!!.removeAll(persistedCategories)
            oldBase.categories!!.addAll(persistedCategories)

            val persistedVariables = realm.copyToRealmOrUpdate(base.variables)
            oldBase.variables!!.removeAll(persistedVariables)
            oldBase.variables!!.addAll(persistedVariables)
        }
    }

    val categories: RealmList<Category>
        get() = base.categories!!

    val variables: RealmList<Variable>
        get() = base.variables!!

    fun deleteShortcut(shortcut: Shortcut) {
        realm.executeTransaction { realm ->
            realm
                    .where(PendingExecution::class.java)
                    .equalTo(PendingExecution.FIELD_SHORTCUT_ID, shortcut.id)
                    .findAll()
                    .deleteAllFromRealm()
            shortcut.headers!!.deleteAllFromRealm()
            shortcut.parameters!!.deleteAllFromRealm()
            shortcut.deleteFromRealm()
        }
    }

    fun moveShortcut(shortcut: Shortcut, position: Int) {
        realm.executeTransaction {
            for (category in categories) {
                var oldPosition = -1
                var i = 0
                for (s in category.shortcuts!!) {
                    if (s.id == shortcut.id) {
                        oldPosition = i
                        break
                    }
                    i++
                }
                if (oldPosition != -1) {
                    category.shortcuts!!.move(oldPosition, position)
                } else {
                    category.shortcuts!!.remove(shortcut)
                }
            }
        }
    }

    fun moveShortcut(shortcut: Shortcut, targetCategory: Category) {
        realm.executeTransaction(Realm.Transaction {
            for (category in categories) {
                if (category.id == targetCategory.id) {
                    for (s in category.shortcuts!!) {
                        if (s.id == shortcut.id) {
                            return@Transaction
                        }
                    }
                } else {
                    category.shortcuts!!.remove(shortcut)
                }
            }
            targetCategory.shortcuts!!.add(shortcut)
        })
    }

    fun createCategory(name: String) {
        realm.executeTransaction { realm ->
            val categories = base.categories
            var category = Category.createNew(name)
            category.id = generateId(Category::class.java)
            category = realm.copyToRealm(category)
            categories!!.add(category)
        }
    }

    fun renameCategory(category: Category, newName: String) {
        realm.executeTransaction { category.name = newName }
    }

    fun setLayoutType(category: Category, layoutType: String) {
        realm.executeTransaction { category.layoutType = layoutType }
    }

    fun moveCategory(category: Category, position: Int) {
        realm.executeTransaction {
            val categories = base.categories
            val oldPosition = categories!!.indexOf(category)
            categories.move(oldPosition, position)
        }
    }

    fun deleteCategory(category: Category) {
        realm.executeTransaction {
            for (shortcut in category.shortcuts!!) {
                shortcut.headers!!.deleteAllFromRealm()
                shortcut.parameters!!.deleteAllFromRealm()
            }
            category.shortcuts!!.deleteAllFromRealm()
            category.deleteFromRealm()
        }
    }

    fun deleteVariable(variable: Variable) {
        realm.executeTransaction {
            variable.options!!.deleteAllFromRealm()
            variable.deleteFromRealm()
        }
    }

    val shortcutsPendingExecution: RealmResults<PendingExecution>
        get() = realm
                .where(PendingExecution::class.java)
                .findAllSorted(PendingExecution.FIELD_ENQUEUED_AT)

    fun createPendingExecution(shortcutId: Long, resolvedVariables: List<ResolvedVariable>, tryNumber: Int = 0, waitUntil: Date? = null) {
        val existingPendingExecutions = realm
                .where(PendingExecution::class.java)
                .equalTo(PendingExecution.FIELD_SHORTCUT_ID, shortcutId)
                .count()

        if (existingPendingExecutions == 0L) {
            realm.executeTransaction {
                realm ->
                realm.copyToRealm(PendingExecution.createNew(shortcutId, resolvedVariables, tryNumber, waitUntil))
            }
        }
    }

    fun removePendingExecution(pendingExecution: PendingExecution) {
        realm.executeTransaction { pendingExecution.deleteFromRealm() }
    }

    fun persist(shortcut: Shortcut): Shortcut {
        if (shortcut.isNew) {
            shortcut.id = generateId(Shortcut::class.java)
        }

        realm.executeTransaction { realm -> realm.copyToRealmOrUpdate(shortcut) }
        return getShortcutById(shortcut.id)
    }

    fun persist(variable: Variable): Variable {
        val isNew = variable.isNew
        if (isNew) {
            variable.id = generateId(Variable::class.java)
        }

        realm.executeTransaction { realm ->
            val newVariable = realm.copyToRealmOrUpdate(variable)
            if (isNew) {
                variables.add(newVariable)
            }
        }
        return getVariableById(variable.id)
    }

    private fun generateId(clazz: Class<out RealmObject>): Long {
        val maxId = realm.where(clazz).max(FIELD_ID)
        val maxIdLong = Math.max(maxId?.toLong() ?: 0, 0)
        return maxIdLong + 1
    }

    val shortcuts: Collection<Shortcut>
        get() = realm
                .where(Shortcut::class.java)
                .notEqualTo(FIELD_ID, Shortcut.TEMPORARY_ID)
                .findAll()

    companion object {

        private const val FIELD_ID = "id"

        fun init(context: Context) {
            Realm.init(context)

            val realm = RealmFactory.realm
            if (realm.where(Base::class.java).count() == 0L) {
                setupBase(context, realm)
            }
            realm.close()
        }

        private fun setupBase(context: Context, realm: Realm) {
            val defaultCategoryName = context.getString(R.string.shortcuts)
            realm.executeTransaction { realm ->
                val defaultCategory = Category.createNew(defaultCategoryName)
                defaultCategory.id = 1

                val newBase = Base()
                newBase.categories = RealmList<Category>()
                newBase.variables = RealmList<Variable>()
                newBase.categories!!.add(defaultCategory)
                realm.copyToRealm(newBase)
            }
        }
    }

}
