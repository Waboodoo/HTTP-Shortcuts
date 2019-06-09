package ch.rmy.android.http_shortcuts.activities.variables

import android.app.Application
import ch.rmy.android.http_shortcuts.data.RealmViewModel
import ch.rmy.android.http_shortcuts.data.Repository.getBase
import ch.rmy.android.http_shortcuts.data.Repository.getVariableById
import ch.rmy.android.http_shortcuts.data.Repository.getVariableByKey
import ch.rmy.android.http_shortcuts.data.Transactions
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.extensions.detachFromRealm
import ch.rmy.android.http_shortcuts.utils.UUIDUtils.newUUID
import io.reactivex.Completable

class VariableEditorViewModel(application: Application) : RealmViewModel(application) {

    var variableId: String? = null
        set(value) {
            field = value
            variable = getDetachedVariable(value)

        }

    private lateinit var variable: Variable

    private fun getDetachedVariable(variableId: String?): Variable =
        if (variableId != null) {
            getVariableById(persistedRealm, variableId)!!.detachFromRealm()
        } else {
            Variable()
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
            variable.id = newUUID()
        }
        return Transactions.commit { realm ->
            val base = getBase(realm) ?: return@commit
            val newVariable = realm.copyToRealmOrUpdate(variable)
            if (isNew) {
                base.variables.add(newVariable)
            }
        }
    }

}