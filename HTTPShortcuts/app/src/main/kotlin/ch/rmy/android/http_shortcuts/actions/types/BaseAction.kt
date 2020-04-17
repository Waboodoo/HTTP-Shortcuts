package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.http.ErrorResponse
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.variables.VariableManager
import io.reactivex.Completable

abstract class BaseAction(val actionType: BaseActionType) {

    open fun perform(context: Context, shortcutId: String, variableManager: VariableManager, response: ShortcutResponse?, responseError: ErrorResponse?, recursionDepth: Int): Completable {
        performBlocking(context, shortcutId, variableManager, response, responseError, recursionDepth)
        return Completable.complete()
    }

    protected open fun performBlocking(context: Context, shortcutId: String, variableManager: VariableManager, response: ShortcutResponse?, responseError: ErrorResponse?, recursionDepth: Int) {

    }

}