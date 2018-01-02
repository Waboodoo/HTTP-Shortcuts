package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.realm.models.Variable
import org.jdeferred.Deferred

open class TextType : BaseVariableType(), AsyncVariableType {

    override val hasTitle = true

    override fun createDialog(context: Context, controller: Controller, variable: Variable, deferredValue: Deferred<String, Unit, Unit>): () -> Unit {
        val builder = BaseVariableType.createDialogBuilder(context, variable, deferredValue)
                .input(null, if (variable.rememberValue) variable.value else "") { _, input ->
                    deferredValue.resolve(input.toString())
                    controller.setVariableValue(variable, input.toString())
                }
        return {
            builder.show()
        }
    }

    override fun createEditorFragment() = TextEditorFragment()

}
