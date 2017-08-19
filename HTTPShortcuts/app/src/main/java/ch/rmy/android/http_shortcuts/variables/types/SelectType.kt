package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.realm.models.Variable
import ch.rmy.android.http_shortcuts.variables.Showable
import org.jdeferred.Deferred

internal class SelectType : BaseVariableType(), AsyncVariableType {

    override fun hasTitle() = true

    override fun createDialog(context: Context, controller: Controller, variable: Variable, deferredValue: Deferred<String, Void, Void>): Showable {
        val items = variable.options!!.map { it.label!! }

        val builder = BaseVariableType.createDialogBuilder(context, variable, deferredValue)
        builder.items(items)
                .itemsCallback { _, _, which, _ ->
                    val value = variable.options!![which].value
                    deferredValue.resolve(value)
                    controller.setVariableValue(variable, value!!)
                }
        return object : Showable {
            override fun show() {
                builder.show()
            }
        }
    }

    override fun createEditorFragment() = SelectEditorFragment()

}
