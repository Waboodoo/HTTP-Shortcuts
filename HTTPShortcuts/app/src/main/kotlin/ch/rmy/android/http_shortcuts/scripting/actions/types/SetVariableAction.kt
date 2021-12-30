package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.RealmFactory
import ch.rmy.android.http_shortcuts.data.Repository
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.extensions.truncate
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import io.reactivex.Completable

class SetVariableAction(val variableKeyOrId: String, val value: String) : BaseAction() {

    private fun validate(variable: Variable, value: String): Boolean {
        if ( variable.isConstant ) return true
        if ( variable.isRememberValueSet ) {
            if ( variable.isText ) return true
            if ( variable.isNumber or variable.isSlider ) {
                if ( value.all { it in '0' .. '9' } ) return true
            }
        }
        return false
    }

    override fun execute(executionContext: ExecutionContext): Completable =
        Completable.fromAction {
            executionContext.variableManager.setVariableValueByKeyOrId(variableKeyOrId, value)

            // TODO: Handle variable persistence in a better way
            RealmFactory.getInstance().createRealm().use { realm ->
                realm.executeTransaction {
                    (
                        Repository.getVariableByKeyOrId(realm, variableKeyOrId)
                            ?.takeIf { validate(it, value) }
                            ?: throw ActionException {
                                it.getString(
                                    R.string.error_variable_not_found_write,
                                    variableKeyOrId,
                                )
                            }
                        )
                        .value = value.truncate(MAX_VARIABLE_LENGTH)
                }
            }
        }

    companion object {

        private const val MAX_VARIABLE_LENGTH = 30000
    }
}
