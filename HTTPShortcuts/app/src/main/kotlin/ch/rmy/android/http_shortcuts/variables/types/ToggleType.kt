package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.models.Variable
import io.reactivex.Single

internal class ToggleType : BaseVariableType() {

    private val variablesRepository = VariableRepository()

    override fun resolveValue(context: Context, variable: Variable) =
        Single.defer {
            val options = variable.options?.takeUnlessEmpty() ?: return@defer Single.just("")

            val previousIndex = variable.value?.toIntOrNull() ?: 0
            val index = (previousIndex + 1) % options.size
            variablesRepository.setVariableValue(variable.id, index.toString())
                .toSingleDefault(options[index]!!.value)
        }
}
