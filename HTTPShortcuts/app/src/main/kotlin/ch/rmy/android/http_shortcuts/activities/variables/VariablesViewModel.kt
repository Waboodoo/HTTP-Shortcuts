package ch.rmy.android.http_shortcuts.activities.variables

import android.app.Application
import ch.rmy.android.http_shortcuts.realm.RealmViewModel
import ch.rmy.android.http_shortcuts.realm.Repository
import ch.rmy.android.http_shortcuts.realm.Repository.generateId
import ch.rmy.android.http_shortcuts.realm.Repository.getBase
import ch.rmy.android.http_shortcuts.realm.Repository.getVariableById
import ch.rmy.android.http_shortcuts.realm.commitAsync
import ch.rmy.android.http_shortcuts.realm.detachFromRealm
import ch.rmy.android.http_shortcuts.realm.livedata.ListLiveData
import ch.rmy.android.http_shortcuts.realm.models.Variable
import ch.rmy.android.http_shortcuts.realm.toLiveData
import ch.rmy.android.http_shortcuts.utils.Settings
import ch.rmy.android.http_shortcuts.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.variables.Variables.KEY_MAX_LENGTH
import io.realm.Realm

class VariablesViewModel(application: Application) : RealmViewModel(application) {

    private val settings by lazy { Settings(application.applicationContext) }

    var wasVariableIntroShown: Boolean
        get() = settings.wasVariableIntroShown
        set(value) {
            settings.wasVariableIntroShown = value
        }

    fun getVariables(): ListLiveData<Variable> =
        Repository.getBase(persistedRealm)!!
            .variables
            .toLiveData()

    fun moveVariable(variableId: Long, position: Int) =
        persistedRealm.commitAsync { realm ->
            val variable = getVariableById(realm, variableId) ?: return@commitAsync
            val variables = getBase(realm)?.variables ?: return@commitAsync
            val oldPosition = variables.indexOf(variable)
            variables.move(oldPosition, position)
        }

    fun deleteVariable(variableId: Long) =
        persistedRealm.commitAsync { realm ->
            getVariableById(realm, variableId)?.apply {
                options?.deleteAllFromRealm()
                deleteFromRealm()
            }
        }

    fun duplicateVariable(variableId: Long) =
        persistedRealm.commitAsync { realm ->
            val oldVariable = getVariableById(realm, variableId) ?: return@commitAsync
            val newVariable = oldVariable.detachFromRealm()
            newVariable.id = generateId(realm, Variable::class.java)
            newVariable.key = generateNewKey(realm, oldVariable.key)
            newVariable.options?.forEach {
                it.id = newUUID()
            }

            val base = getBase(realm) ?: return@commitAsync
            val oldPosition = base.variables.indexOf(oldVariable)
            val newPersistedVariable = realm.copyToRealmOrUpdate(newVariable)
            base.variables.add(oldPosition + 1, newPersistedVariable)
        }

    private fun generateNewKey(realm: Realm, oldKey: String): String {
        val base = oldKey.take(KEY_MAX_LENGTH - 1)
        for (i in 2..9) {
            val newKey = "$base$i"
            if (!isVariableKeyInUse(realm, newKey)) {
                return newKey
            }
        }
        throw RuntimeException("Failed to generate new key for variable duplication")
    }

    private fun isVariableKeyInUse(realm: Realm, key: String): Boolean =
        Repository.getVariableByKey(realm, key) != null

}