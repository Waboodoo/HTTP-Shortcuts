package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import ch.rmy.android.http_shortcuts.data.models.Variable
import org.jdeferred2.Deferred

interface AsyncVariableType {

    fun createDialog(context: Context, variable: Variable, deferredValue: Deferred<String, Unit, Unit>): () -> Unit

    val hasTitle: Boolean

}
