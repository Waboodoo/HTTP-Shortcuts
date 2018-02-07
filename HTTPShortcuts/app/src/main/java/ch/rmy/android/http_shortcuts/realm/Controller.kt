package ch.rmy.android.http_shortcuts.realm

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.realm.models.Base
import ch.rmy.android.http_shortcuts.realm.models.Category
import ch.rmy.android.http_shortcuts.realm.models.PendingExecution
import ch.rmy.android.http_shortcuts.realm.models.ResolvedVariable
import ch.rmy.android.http_shortcuts.realm.models.Shortcut
import ch.rmy.android.http_shortcuts.realm.models.Variable
import ch.rmy.android.http_shortcuts.utils.Destroyable
import io.realm.Case
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.kotlin.where
import java.util.*

class Controller : Destroyable {

    private val realm: Realm = realmFactory.createRealm()

    override fun destroy() {
        if (!realm.isClosed) {
            realm.close()
        }
    }

    fun getCategoryById(id: Long): Category? = realm.where<Category>().equalTo(FIELD_ID, id).findFirst()

    fun getShortcutById(id: Long): Shortcut? = realm.where<Shortcut>().equalTo(FIELD_ID, id).findFirst()

    fun getShortcutByName(shortcutName: String): Shortcut? = realm.where<Shortcut>().equalTo(Shortcut.FIELD_NAME, shortcutName, Case.INSENSITIVE).findFirst()

    private fun getVariableById(id: Long): Variable? = realm.where<Variable>().equalTo(FIELD_ID, id).findFirst()

    fun getVariableByKey(key: String): Variable? = realm.where<Variable>().equalTo(Variable.FIELD_KEY, key).findFirst()

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
        get() = realm.where<Base>().findFirst()!!

    fun exportBase(): Base = realm.copyFromRealm(base)

    fun importBase(base: Base) {
        val oldBase = this.base
        realm.executeTransaction { realm ->
            val persistedCategories = realm.copyToRealmOrUpdate(base.categories)
            oldBase.categories.removeAll(persistedCategories)
            oldBase.categories.addAll(persistedCategories)

            val persistedVariables = realm.copyToRealmOrUpdate(base.variables)
            oldBase.variables.removeAll(persistedVariables)
            oldBase.variables.addAll(persistedVariables)
        }
    }

    val categories: RealmList<Category>
        get() = base.categories

    val variables: RealmList<Variable>
        get() = base.variables

    fun deleteShortcut(shortcut: Shortcut) {
        realm.executeTransaction { realm ->
            realm
                    .where<PendingExecution>()
                    .equalTo(PendingExecution.FIELD_SHORTCUT_ID, shortcut.id)
                    .findAll()
                    .deleteAllFromRealm()
            shortcut.headers.deleteAllFromRealm()
            shortcut.parameters.deleteAllFromRealm()
            shortcut.deleteFromRealm()
        }
    }

    fun moveShortcut(shortcut: Shortcut, position: Int) {
        realm.executeTransaction {
            for (category in categories) {
                var oldPosition = -1
                var i = 0
                for (s in category.shortcuts) {
                    if (s.id == shortcut.id) {
                        oldPosition = i
                        break
                    }
                    i++
                }
                if (oldPosition != -1) {
                    category.shortcuts.move(oldPosition, position)
                } else {
                    category.shortcuts.remove(shortcut)
                }
            }
        }
    }

    fun moveShortcut(shortcut: Shortcut, targetCategory: Category) {
        realm.executeTransaction(Realm.Transaction {
            for (category in categories) {
                if (category.id == targetCategory.id) {
                    for (s in category.shortcuts) {
                        if (s.id == shortcut.id) {
                            return@Transaction
                        }
                    }
                } else {
                    category.shortcuts.remove(shortcut)
                }
            }
            targetCategory.shortcuts.add(shortcut)
        })
    }

    fun createCategory(name: String) {
        realm.executeTransaction { realm ->
            val categories = base.categories
            var category = Category.createNew(name)
            category.id = generateId(Category::class.java)
            category = realm.copyToRealm(category)
            categories.add(category)
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
            val oldPosition = categories.indexOf(category)
            categories.move(oldPosition, position)
        }
    }

    fun deleteCategory(category: Category) {
        realm.executeTransaction {
            for (shortcut in category.shortcuts) {
                shortcut.headers.deleteAllFromRealm()
                shortcut.parameters.deleteAllFromRealm()
            }
            category.shortcuts.deleteAllFromRealm()
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
                .where<PendingExecution>()
                .sort(PendingExecution.FIELD_ENQUEUED_AT)
                .findAll()

    fun createPendingExecution(shortcutId: Long, resolvedVariables: List<ResolvedVariable>, tryNumber: Int = 0, waitUntil: Date? = null) {
        val existingPendingExecutions = realm
                .where<PendingExecution>()
                .equalTo(PendingExecution.FIELD_SHORTCUT_ID, shortcutId)
                .count()

        if (existingPendingExecutions == 0L) {
            realm.executeTransaction { realm ->
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
        return getShortcutById(shortcut.id)!!
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
        return getVariableById(variable.id)!!
    }

    private fun generateId(clazz: Class<out RealmObject>): Long {
        val maxId = realm.where(clazz).max(FIELD_ID)
        val maxIdLong = Math.max(maxId?.toLong() ?: 0, 0)
        return maxIdLong + 1
    }

    val shortcuts: Collection<Shortcut>
        get() = realm
                .where<Shortcut>()
                .notEqualTo(FIELD_ID, Shortcut.TEMPORARY_ID)
                .findAll()

    companion object {

        private const val FIELD_ID = "id"

        private lateinit var realmFactory: RealmFactory

        fun init(context: Context) {
            Realm.init(context)
            realmFactory = RealmFactory(EncryptionHelper(context).encryptionKey)

            realmFactory.createRealm().use { realm ->
                if (realm.where<Base>().count() == 0L) {
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
                realm.copyToRealm(newBase)
            }
        }
    }

}
