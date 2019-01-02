package ch.rmy.android.http_shortcuts.activities.variables

import android.app.Application
import ch.rmy.android.http_shortcuts.realm.RealmViewModel
import ch.rmy.android.http_shortcuts.realm.Repository.generateId
import ch.rmy.android.http_shortcuts.realm.Repository.getBase
import ch.rmy.android.http_shortcuts.realm.Repository.getVariableById
import ch.rmy.android.http_shortcuts.realm.Repository.getVariableByKey

import ch.rmy.android.http_shortcuts.realm.commitAsync
import ch.rmy.android.http_shortcuts.realm.detachFromRealm
import ch.rmy.android.http_shortcuts.realm.models.Variable
import io.reactivex.Completable

class VariableEditorViewModel(application: Application) : RealmViewModel(application) {

    var variableId: Long? = null
        set(value) {
            field = value
            variable = getDetachedVariable(value)

        }

    private lateinit var variable: Variable

    private fun getDetachedVariable(variableId: Long?): Variable =
        if (variableId != null) {
            getVariableById(persistedRealm, variableId)!!.detachFromRealm()
        } else {
            Variable.createNew()
        }

    fun getVariable(): Variable = variable

    fun hasChanges(): Boolean = !variable.isSameAs(getDetachedVariable(variableId))

    fun isKeyAlreadyInUsed(): Boolean {
        val otherVariable = getVariableByKey(persistedRealm, variable.key)
        return otherVariable != null && otherVariable.id != variable.id
    }

    fun save(): Completable {
        val isNew = variable.isNew
        if (isNew) {
            variable.id = generateId(persistedRealm, Variable::class.java)
        }
        return persistedRealm.commitAsync { realm ->
            val base = getBase(realm) ?: return@commitAsync
            val newVariable = realm.copyToRealmOrUpdate(variable)
            if (isNew) {
                base.variables.add(newVariable)
            }
        }
    }

}