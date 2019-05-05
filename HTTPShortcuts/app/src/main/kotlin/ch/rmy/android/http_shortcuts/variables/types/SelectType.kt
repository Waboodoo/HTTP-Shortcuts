package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import ch.rmy.android.http_shortcuts.data.Controller
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.extensions.mapFor
import ch.rmy.android.http_shortcuts.utils.showIfPossible
import org.jdeferred2.Deferred

internal class SelectType : BaseVariableType(), AsyncVariableType {

    override val hasTitle = true

    override fun createDialog(context: Context, controller: Controller, variable: Variable, deferredValue: Deferred<String, Unit, Unit>): () -> Unit {
        val builder = createDialogBuilder(context, variable, deferredValue)
            .mapFor(variable.options!!) { builder, option ->
                builder.item(option.labelOrValue) {
                    if (variable.isValid) {
                        deferredValue.resolve(option.value)
                        controller.setVariableValue(variable.id, option.value).subscribe()
                    }
                }
            }
        return {
            builder.showIfPossible()
        }
    }

    override fun createEditorFragment() = SelectEditorFragment()

}
