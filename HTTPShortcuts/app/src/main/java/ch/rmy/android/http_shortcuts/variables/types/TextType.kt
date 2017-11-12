package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.realm.models.Variable
import org.jdeferred.Deferred

open class TextType : BaseVariableType(), AsyncVariableType {

    override fun hasTitle() = true

    override fun createDialog(context: Context, controller: Controller, variable: Variable, deferredValue: Deferred<String, Void, Void>): () -> Unit {
        val builder = BaseVariableType.createDialogBuilder(context, variable, deferredValue)
        builder.input(null, if (variable.rememberValue) variable.value else "") { _, input ->
            deferredValue.resolve(input.toString())
            controller.setVariableValue(variable, input.toString())
        }
        return {
            builder.show()
        }
    }

    override fun createEditorFragment() = TextEditorFragment()

}
