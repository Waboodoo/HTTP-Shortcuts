package ch.rmy.android.http_shortcuts.activities.variables

import android.app.Application
import ch.rmy.android.http_shortcuts.realm.ListLiveData
import ch.rmy.android.http_shortcuts.realm.RealmViewModel
import ch.rmy.android.http_shortcuts.realm.Repository
import ch.rmy.android.http_shortcuts.realm.Repository.getBase
import ch.rmy.android.http_shortcuts.realm.Repository.getVariableById
import ch.rmy.android.http_shortcuts.realm.commitAsync
import ch.rmy.android.http_shortcuts.realm.models.Variable
import ch.rmy.android.http_shortcuts.realm.toLiveData
import ch.rmy.android.http_shortcuts.utils.Settings

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
}