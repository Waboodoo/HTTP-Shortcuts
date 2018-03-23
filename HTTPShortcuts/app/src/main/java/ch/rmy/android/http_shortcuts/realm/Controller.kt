package ch.rmy.android.http_shortcuts.realm

import android.content.Context
import ch.rmy.android.http_shortcuts.BuildConfig
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.realm.models.Base
import ch.rmy.android.http_shortcuts.realm.models.Category
import ch.rmy.android.http_shortcuts.realm.models.PendingExecution
import ch.rmy.android.http_shortcuts.realm.models.ResolvedVariable
import ch.rmy.android.http_shortcuts.realm.models.Shortcut
import ch.rmy.android.http_shortcuts.realm.models.Variable
import ch.rmy.android.http_shortcuts.utils.Destroyable
import ch.rmy.android.http_shortcuts.utils.filter
import io.realm.Case
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.kotlin.where
import org.jdeferred.Promise
import java.util.*

class Controller : Destroyable {

    private val realm: Realm = realmFactory.createRealm()

    override fun destroy() {
        if (!realm.isClosed) {
            realm.close()
        }
    }

    fun getCategoryById(id: Long) = getCategoryById(realm, id)

    private fun getCategoryById(realm: Realm, categoryId: Long) = realm
            .where<Category>()
            .equalTo(FIELD_ID, categoryId)
            .findFirst()

    fun getShortcutById(id: Long) = getShortcutById(realm, id)

    private fun getShortcutById(realm: Realm, shortcutId: Long) = realm
            .where<Shortcut>()
            .equalTo(FIELD_ID, shortcutId)
            .findFirst()

    fun getShortcutByName(shortcutName: String): Shortcut? = realm.where<Shortcut>().equalTo(Shortcut.FIELD_NAME, shortcutName, Case.INSENSITIVE).findFirst()

    private fun getVariableById(id: Long) = getVariableById(realm, id)

    private fun getVariableById(realm: Realm, variableId: Long) = realm
            .where<Variable>()
            .equalTo(FIELD_ID, variableId)
            .findFirst()

    fun getVariableByKey(key: String): Variable? = realm.where<Variable>().equalTo(Variable.FIELD_KEY, key).findFirst()

    fun setVariableValue(variableId: Long, value: String) =
            realm.commitAsync { realm ->
                getVariableById(realm, variableId)?.value = value
            }

    fun resetVariableValues(variableIds: List<Long>) =
            realm.commitAsync { realm ->
                variableIds.forEach { variableId ->
                    getVariableById(realm, variableId)?.value = ""
                }
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
        get() = getBase(realm)!!

    private fun getBase(realm: Realm) = realm.where<Base>().findFirst()

    fun exportBase(): Base = realm.copyFromRealm(base)

    fun importBaseSynchronously(base: Base) {
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

    fun deleteShortcut(shortcutId: Long) =
            realm.commitAsync { realm ->
                val shortcut = getShortcutById(realm, shortcutId) ?: return@commitAsync
                realm
                        .where<PendingExecution>()
                        .equalTo(PendingExecution.FIELD_SHORTCUT_ID, shortcutId)
                        .findAll()
                        .deleteAllFromRealm()
                shortcut.headers.deleteAllFromRealm()
                shortcut.parameters.deleteAllFromRealm()
                shortcut.deleteFromRealm()
            }

    fun moveShortcut(shortcutId: Long, targetPosition: Int? = null, targetCategoryId: Long? = null) =
            realm.commitAsync { realm ->
                val shortcut = getShortcutById(realm, shortcutId) ?: return@commitAsync
                val categories = getBase(realm)?.categories ?: return@commitAsync
                val targetCategory = if (targetCategoryId != null) {
                    getCategoryById(realm, targetCategoryId)
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

    fun createCategory(name: String) =
            realm.commitAsync { realm ->
                val base = getBase(realm) ?: return@commitAsync
                val categories = base.categories
                val category = Category.createNew(name)
                category.id = generateId(realm, Category::class.java)
                categories.add(realm.copyToRealm(category))
            }

    fun renameCategory(categoryId: Long, newName: String) =
            realm.commitAsync { realm ->
                getCategoryById(realm, categoryId)?.name = newName
            }

    fun setLayoutType(categoryId: Long, layoutType: String) =
            realm.commitAsync { realm ->
                getCategoryById(realm, categoryId)?.layoutType = layoutType
            }

    fun moveCategory(categoryId: Long, position: Int) =
            realm.commitAsync { realm ->
                val base = getBase(realm) ?: return@commitAsync
                val category = getCategoryById(realm, categoryId) ?: return@commitAsync
                val categories = base.categories
                val oldPosition = categories.indexOf(category)
                categories.move(oldPosition, position)
            }

    fun deleteCategory(categoryId: Long) =
            realm.commitAsync { realm ->
                val category = getCategoryById(realm, categoryId) ?: return@commitAsync
                for (shortcut in category.shortcuts) {
                    shortcut.headers.deleteAllFromRealm()
                    shortcut.parameters.deleteAllFromRealm()
                }
                category.shortcuts.deleteAllFromRealm()
                category.deleteFromRealm()
            }

    fun deleteVariable(variableId: Long) =
            realm.commitAsync { realm ->
                val variable = getVariableById(realm, variableId) ?: return@commitAsync
                variable.options?.deleteAllFromRealm()
                variable.deleteFromRealm()
            }

    val shortcutsPendingExecution: RealmResults<PendingExecution>
        get() = realm
                .where<PendingExecution>()
                .sort(PendingExecution.FIELD_ENQUEUED_AT)
                .findAll()

    fun createPendingExecution(shortcutId: Long, resolvedVariables: List<ResolvedVariable>, tryNumber: Int = 0, waitUntil: Date? = null) =
            realm.commitAsync { realm ->
                val existingPendingExecutions = realm
                        .where<PendingExecution>()
                        .equalTo(PendingExecution.FIELD_SHORTCUT_ID, shortcutId)
                        .count()

                if (existingPendingExecutions == 0L) {
                    realm.copyToRealm(PendingExecution.createNew(shortcutId, resolvedVariables, tryNumber, waitUntil))
                }
            }

    fun removePendingExecution(pendingExecution: PendingExecution) =
            realm.commitAsync { pendingExecution.deleteFromRealm() }

    fun removePendingExecutionSynchronously(pendingExecution: PendingExecution) {
        realm.executeTransaction { pendingExecution.deleteFromRealm() }
    }

    fun persist(shortcut: Shortcut): Promise<Shortcut, Throwable, Unit> {
        val isNew = shortcut.isNew
        if (isNew) {
            shortcut.id = generateId(realm, Shortcut::class.java)
        }
        return realm.commitAsync { realm ->
            val newShortcut = realm.copyToRealmOrUpdate(shortcut)
            if (isNew) {
                // TODO: Attach to correct category
            }
        }
                .filter { getShortcutById(shortcut.id)!! }
    }

    fun persist(variable: Variable): Promise<Variable, Throwable, Unit> {
        val isNew = variable.isNew
        if (isNew) {
            variable.id = generateId(realm, Variable::class.java)
        }
        return realm.commitAsync { realm ->
            val newVariable = realm.copyToRealmOrUpdate(variable)
            if (isNew) {
                variables.add(newVariable)
            }
        }
                .filter { getVariableById(variable.id)!! }
    }

    private fun generateId(realm: Realm, clazz: Class<out RealmObject>): Long {
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
            realmFactory = RealmFactory(BuildConfig.REALM_ENCRYPTION_KEY.toByteArray())

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
                it.copyToRealm(newBase)
            }
        }
    }

}
