package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import ch.rmy.android.http_shortcuts.data.models.Variable
import io.reactivex.Single

internal class ConstantType : BaseVariableType() {

    override fun createEditorFragment() = ConstantEditorFragment()

    override fun resolveValue(context: Context, variable: Variable) =
        Single.fromCallable {
            variable.value!!
        }

}
