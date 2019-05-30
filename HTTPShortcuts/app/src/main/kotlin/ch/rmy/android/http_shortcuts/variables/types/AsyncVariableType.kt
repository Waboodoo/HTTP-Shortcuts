package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import ch.rmy.android.http_shortcuts.data.models.Variable
import io.reactivex.Single

interface AsyncVariableType {

    fun resolveValue(context: Context, variable: Variable): Single<String>

    val hasTitle: Boolean

}
