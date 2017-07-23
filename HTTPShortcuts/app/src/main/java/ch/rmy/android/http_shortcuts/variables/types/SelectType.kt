package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.realm.models.Variable
import ch.rmy.android.http_shortcuts.variables.Showable
import org.jdeferred.Deferred
import java.util.*

internal class SelectType : BaseVariableType(), AsyncVariableType {

    override fun hasTitle(): Boolean {
        return true
    }

    override fun createDialog(context: Context, controller: Controller, variable: Variable, deferredValue: Deferred<String, Void, Void>): Showable {
        val items = ArrayList<CharSequence>()
        for (option in variable.options!!) {
            items.add(option.label!!)
        }

        val builder = BaseVariableType.Companion.createDialogBuilder(context, variable, deferredValue)
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
