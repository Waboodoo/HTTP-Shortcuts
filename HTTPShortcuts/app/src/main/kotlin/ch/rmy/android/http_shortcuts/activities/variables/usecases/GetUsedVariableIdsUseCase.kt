package ch.rmy.android.http_shortcuts.activities.variables.usecases

import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.enums.VariableType
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.VariableResolver
import ch.rmy.android.http_shortcuts.variables.Variables
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class GetUsedVariableIdsUseCase(
    private val shortcutRepository: ShortcutRepository,
    private val variableRepository: VariableRepository,
) {

    operator fun invoke(): Single<Set<String>> =
        variableRepository.getVariables()
            .flatMap { variables ->
                shortcutRepository.getShortcuts().map { shortcuts ->
                    determineVariablesInUse(variables, shortcuts)
                }
            }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())

    private fun determineVariablesInUse(variables: List<Variable>, shortcuts: List<Shortcut>): Set<String> {
        val variableManager = VariableManager(variables)
        return shortcuts
            .flatMap { shortcut ->
                VariableResolver.extractVariableIds(shortcut, variableManager)
            }
            .plus(getVariablesInUseInVariables(variables))
            .toSet()
    }

    private fun getVariablesInUseInVariables(variables: List<Variable>): List<String> =
        variables.flatMap(::getVariablesInUseInVariable)

    private fun getVariablesInUseInVariable(variable: Variable): Set<String> =
        when (variable.variableType) {
            VariableType.CONSTANT -> variable.value?.let(Variables::extractVariableIds) ?: emptySet()
            VariableType.SELECT,
            VariableType.TOGGLE,
            -> {
                variable.options
                    ?.flatMap { option ->
                        Variables.extractVariableIds(option.value)
                    }
                    ?.toSet()
                    ?: emptySet()
            }
            else -> emptySet()
        }
}
