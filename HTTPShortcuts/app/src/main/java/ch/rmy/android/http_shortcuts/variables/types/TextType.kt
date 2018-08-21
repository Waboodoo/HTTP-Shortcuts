package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.realm.models.Variable
import ch.rmy.android.http_shortcuts.utils.showIfPossible
import org.jdeferred2.Deferred

open class TextType : BaseVariableType(), AsyncVariableType {

    override val hasTitle = true

    override fun createDialog(context: Context, controller: Controller, variable: Variable, deferredValue: Deferred<String, Unit, Unit>): () -> Unit {
        val builder = BaseVariableType.createDialogBuilder(context, variable, deferredValue)
                .toDialogBuilder()
                .input(null, if (variable.rememberValue) variable.value else "") { _, input ->
                    if (variable.isValid) {
                        deferredValue.resolve(input.toString())
                        controller.setVariableValue(variable.id, input.toString()).subscribe()
                    }
                }
        return {
            builder.showIfPossible()
        }
    }

    override fun createEditorFragment() = TextEditorFragment()

}
