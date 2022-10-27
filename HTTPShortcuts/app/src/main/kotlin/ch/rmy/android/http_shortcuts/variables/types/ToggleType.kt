package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.models.VariableModel
import javax.inject.Inject

class ToggleType : BaseVariableType() {

    @Inject
    lateinit var variablesRepository: VariableRepository

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override suspend fun resolveValue(context: Context, variable: VariableModel): String {
        val options = variable.options?.takeUnlessEmpty() ?: return ""

        val previousIndex = variable.value?.toIntOrNull()?.coerceAtLeast(0) ?: 0
        val index = (previousIndex + 1) % options.size
        variablesRepository.setVariableValue(variable.id, index.toString())
        return options[index]!!.value
    }
}
