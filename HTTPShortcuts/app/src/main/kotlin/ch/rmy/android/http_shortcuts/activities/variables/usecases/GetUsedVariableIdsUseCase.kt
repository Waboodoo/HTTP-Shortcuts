package ch.rmy.android.http_shortcuts.activities.variables.usecases

import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.enums.VariableType
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.data.models.VariableModel
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.VariableResolver
import ch.rmy.android.http_shortcuts.variables.Variables
import javax.inject.Inject

class GetUsedVariableIdsUseCase
@Inject
constructor(
    private val shortcutRepository: ShortcutRepository,
    private val variableRepository: VariableRepository,
) {

    suspend operator fun invoke(shortcutId: ShortcutId?) =
        invoke(shortcutId?.let(::setOf))

    suspend operator fun invoke(shortcutIds: Collection<ShortcutId>? = null): Set<VariableId> {
        val variables = variableRepository.getVariables()
        val shortcuts = if (shortcutIds != null) {
            shortcutRepository.getShortcutsByIds(shortcutIds)
        } else {
            shortcutRepository.getShortcuts()
        }
        return determineVariablesInUse(variables, shortcuts)
    }

    private fun determineVariablesInUse(variables: List<VariableModel>, shortcuts: List<ShortcutModel>): Set<VariableId> {
        val variableManager = VariableManager(variables)
        return shortcuts
            .flatMap { shortcut ->
                VariableResolver.extractVariableIds(shortcut, variableManager)
            }
            .plus(getVariablesInUseInVariables(variables))
            .toSet()
    }

    private fun getVariablesInUseInVariables(variables: List<VariableModel>): List<VariableId> =
        variables.flatMap(::getVariablesInUseInVariable)

    private fun getVariablesInUseInVariable(variable: VariableModel): Set<VariableId> =
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
