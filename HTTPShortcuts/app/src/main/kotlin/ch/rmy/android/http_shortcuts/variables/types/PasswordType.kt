package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import android.text.InputType
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.utils.showIfPossible
import org.jdeferred2.Deferred

class PasswordType : TextType() {

    override fun createDialog(context: Context, variable: Variable, deferredValue: Deferred<String, Unit, Unit>): () -> Unit {
        val builder = createDialogBuilder(context, variable, deferredValue)
            .toDialogBuilder()
            .input(null, if (variable.rememberValue) variable.value else "") { _, input ->
                deferredValue.resolve(input.toString())
            }
            .inputType(InputType.TYPE_TEXT_VARIATION_PASSWORD)
        return {
            builder.showIfPossible()
        }
    }

}
