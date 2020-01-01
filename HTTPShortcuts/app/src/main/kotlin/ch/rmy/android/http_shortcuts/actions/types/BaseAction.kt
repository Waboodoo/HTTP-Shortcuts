package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.variables.VariableManager
import com.android.volley.VolleyError
import io.reactivex.Completable

abstract class BaseAction(
    val actionType: BaseActionType,
    data: Map<String, String>
) {

    protected val internalData = data.toMutableMap()

    open fun perform(context: Context, shortcutId: String, variableManager: VariableManager, response: ShortcutResponse?, volleyError: VolleyError?, recursionDepth: Int): Completable {
        performBlocking(context, shortcutId, variableManager, response, volleyError, recursionDepth)
        return Completable.complete()
    }

    protected open fun performBlocking(context: Context, shortcutId: String, variableManager: VariableManager, response: ShortcutResponse?, volleyError: VolleyError?, recursionDepth: Int) {

    }

}