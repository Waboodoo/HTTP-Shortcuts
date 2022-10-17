package ch.rmy.android.http_shortcuts.usecases

import androidx.annotation.CheckResult
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import javax.inject.Inject

class KeepVariablePlaceholderProviderUpdatedUseCase
@Inject
constructor(
    private val variableRepository: VariableRepository,
    private val variablePlaceholderProvider: VariablePlaceholderProvider,
) {
    @CheckResult
    suspend operator fun invoke(onUpdated: (() -> Unit)? = null) {
        variableRepository.getObservableVariables()
            .collect { variables ->
                variablePlaceholderProvider.applyVariables(variables)
                onUpdated?.invoke()
            }
    }
}
