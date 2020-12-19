package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import io.reactivex.Single

class GetVariableAction(val variableKeyOrId: String) : BaseAction() {

    override fun executeForValue(executionContext: ExecutionContext): Single<String> =
        Single.fromCallable {
            executionContext.variableManager.getVariableValueByKeyOrId(variableKeyOrId)
                ?: throw ActionException {
                    it.getString(R.string.error_variable_not_found_read, variableKeyOrId)
                }
        }

}