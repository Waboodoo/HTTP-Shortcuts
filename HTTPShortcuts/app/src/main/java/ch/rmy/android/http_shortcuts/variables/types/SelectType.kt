package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.realm.models.Variable
import org.jdeferred.Deferred

internal class SelectType : BaseVariableType(), AsyncVariableType {

    override val hasTitle = true

    override fun createDialog(context: Context, controller: Controller, variable: Variable, deferredValue: Deferred<String, Unit, Unit>): () -> Unit {
        val items = variable.options!!.map { it.label!! }

        val builder = BaseVariableType.createDialogBuilder(context, variable, deferredValue)
                .items(items)
                .itemsCallback { _, _, which, _ ->
                    val value = variable.options!![which].value
                    deferredValue.resolve(value)
                    controller.setVariableValue(variable, value!!)
                }
        return {
            builder.show()
        }
    }

    override fun createEditorFragment() = SelectEditorFragment()

}
