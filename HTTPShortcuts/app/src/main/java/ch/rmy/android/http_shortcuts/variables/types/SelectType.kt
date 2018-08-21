package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.realm.models.Variable
import ch.rmy.android.http_shortcuts.utils.mapFor
import ch.rmy.android.http_shortcuts.utils.showIfPossible
import org.jdeferred2.Deferred

internal class SelectType : BaseVariableType(), AsyncVariableType {

    override val hasTitle = true

    override fun createDialog(context: Context, controller: Controller, variable: Variable, deferredValue: Deferred<String, Unit, Unit>): () -> Unit {
        val builder = BaseVariableType.createDialogBuilder(context, variable, deferredValue)
                .mapFor(variable.options!!) { builder, option ->
                    builder.item(option.label) {
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
