package ch.rmy.android.http_shortcuts.usecases

import androidx.annotation.CheckResult
import ch.rmy.android.framework.extensions.toDestroyable
import ch.rmy.android.framework.utils.Destroyable
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class KeepVariablePlaceholderProviderUpdatedUseCase
@Inject
constructor(
    private val variableRepository: VariableRepository,
    private val variablePlaceholderProvider: VariablePlaceholderProvider,
) {
    @CheckResult
    operator fun invoke(onUpdated: (() -> Unit)? = null): Destroyable =
        variableRepository.getObservableVariables()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { variables ->
                variablePlaceholderProvider.applyVariables(variables)
                onUpdated?.invoke()
            }
            .toDestroyable()
}
