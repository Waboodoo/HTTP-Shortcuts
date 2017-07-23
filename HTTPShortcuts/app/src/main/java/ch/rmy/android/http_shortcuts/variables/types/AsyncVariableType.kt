package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.realm.models.Variable
import ch.rmy.android.http_shortcuts.variables.Showable
import org.jdeferred.Deferred

interface AsyncVariableType {

    fun createDialog(context: Context, controller: Controller, variable: Variable, deferredValue: Deferred<String, Void, Void>): Showable

    fun hasTitle(): Boolean

}
