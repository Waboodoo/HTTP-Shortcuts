package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import android.text.InputType
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.realm.models.Variable
import ch.rmy.android.http_shortcuts.variables.Showable
import org.jdeferred.Deferred

internal class NumberType : TextType() {

    override fun createDialog(context: Context, controller: Controller, variable: Variable, deferredValue: Deferred<String, Void, Void>): Showable {
        val builder = createDialogBuilder(context, variable, deferredValue)
        builder.input(null, if (variable.rememberValue) variable.value else "") { _, input ->
            deferredValue.resolve(input.toString())
            controller.setVariableValue(variable, input.toString())
        }.inputType(InputType.TYPE_CLASS_NUMBER)
        return {
            builder.show()
        }
    }

}
