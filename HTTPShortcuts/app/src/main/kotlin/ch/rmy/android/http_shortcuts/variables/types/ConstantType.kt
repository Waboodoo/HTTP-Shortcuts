package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import ch.rmy.android.http_shortcuts.data.models.VariableModel
import io.reactivex.Single

class ConstantType : BaseVariableType() {

    override fun resolveValue(context: Context, variable: VariableModel) =
        Single.fromCallable {
            variable.value!!
        }
}
