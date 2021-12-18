package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import ch.rmy.android.http_shortcuts.data.Commons
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.extensions.takeUnlessEmpty
import io.reactivex.Single

internal class ToggleType : BaseVariableType() {

    override fun resolveValue(context: Context, variable: Variable) =
        Single.defer {
            val options = variable.options?.takeUnlessEmpty() ?: return@defer Single.just("")

            val previousIndex = variable.value?.toIntOrNull() ?: 0
            val index = (previousIndex + 1) % options.size
            Commons.setVariableValue(variable.id, index.toString())
                .toSingleDefault(options[index]!!.value)
        }

    override fun createEditorFragment() = ToggleEditorFragment()
}
