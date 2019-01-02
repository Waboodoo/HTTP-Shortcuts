package ch.rmy.android.http_shortcuts.realm

import ch.rmy.android.http_shortcuts.realm.Repository.generateId
import ch.rmy.android.http_shortcuts.realm.models.Base
import ch.rmy.android.http_shortcuts.realm.models.Category
import ch.rmy.android.http_shortcuts.realm.models.PendingExecution
import ch.rmy.android.http_shortcuts.realm.models.Shortcut
import ch.rmy.android.http_shortcuts.realm.models.Variable
import ch.rmy.android.http_shortcuts.utils.Destroyable
import io.reactivex.Completable
import io.reactivex.Single
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmResults
import java.io.Closeable
import java.util.*

class Controller : Destroyable, Closeable {

    private val realm: Realm = RealmFactory.getInstance().createRealm()

    override fun destroy() {
        if (!realm.isClosed) {
            realm.close()
        }
    }

    override fun close() = destroy()

    fun getShortcuts() = Repository.getShortcuts(realm)

    fun getCategories(): RealmList<Category> = getBase().categories

    fun getVariables(): RealmList<Variable> = getBase().variables

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

    fun renameShortcut(shortcutId: Long, newName: String) =
        realm.commitAsync { realm ->
            Repository.getShortcutById(realm, shortcutId)?.name = newName
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

    fun persist(shortcut: Shortcut): Single<Shortcut> {
        if (shortcut.isNew) {
            shortcut.id = generateId(realm, Shortcut::class.java)
        }
        return realm.commitAsync { realm ->
            realm.copyToRealmOrUpdate(shortcut)
        }
            .toSingle {
                getShortcutById(shortcut.id)!!
            }
    }

    fun persist(variable: Variable): Completable {
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
    }

}
