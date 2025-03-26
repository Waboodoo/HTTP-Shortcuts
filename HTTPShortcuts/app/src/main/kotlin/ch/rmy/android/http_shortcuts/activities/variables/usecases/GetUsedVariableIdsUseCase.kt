package ch.rmy.android.http_shortcuts.activities.variables.usecases

import ch.rmy.android.http_shortcuts.data.domains.request_headers.RequestHeaderRepository
import ch.rmy.android.http_shortcuts.data.domains.request_parameters.RequestParameterRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.enums.VariableType
import ch.rmy.android.http_shortcuts.data.models.RequestHeader
import ch.rmy.android.http_shortcuts.data.models.RequestParameter
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.extensions.getRequestParametersForShortcuts
import ch.rmy.android.http_shortcuts.extensions.ids
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.VariableResolver
import ch.rmy.android.http_shortcuts.variables.Variables
import ch.rmy.android.http_shortcuts.variables.types.SelectType
import ch.rmy.android.http_shortcuts.variables.types.ToggleType
import javax.inject.Inject

class GetUsedVariableIdsUseCase
@Inject
constructor(
    private val shortcutRepository: ShortcutRepository,
    private val requestHeaderRepository: RequestHeaderRepository,
    private val requestParameterRepository: RequestParameterRepository,
    private val variableRepository: VariableRepository,
) {

    suspend operator fun invoke(shortcutId: ShortcutId?) =
        invoke(shortcutId?.let(::setOf))

    suspend operator fun invoke(shortcutIds: Collection<ShortcutId>? = null): Set<VariableId> {
        val shortcuts = if (shortcutIds != null) {
            shortcutRepository.getShortcutsByIds(shortcutIds)
        } else {
            shortcutRepository.getShortcuts()
        }
        return determineVariablesInUse(
            variables = variableRepository.getVariables(),
            shortcuts = shortcuts,
            headersByShortcutId = requestHeaderRepository.getRequestHeadersByShortcutIds(shortcuts.ids()),
            parametersByShortcutId = requestParameterRepository.getRequestParametersForShortcuts(shortcuts),
        )
    }

    private fun determineVariablesInUse(
        variables: List<Variable>,
        shortcuts: List<Shortcut>,
        headersByShortcutId: Map<ShortcutId, List<RequestHeader>>,
        parametersByShortcutId: Map<ShortcutId, List<RequestParameter>>,
    ): Set<VariableId> {
        val variableManager = VariableManager(variables)
        return shortcuts
            .flatMap { shortcut ->
                VariableResolver.extractVariableIdsIncludingScripting(
                    shortcut = shortcut,
                    headers = headersByShortcutId[shortcut.id] ?: emptyList(),
                    parameters = parametersByShortcutId[shortcut.id] ?: emptyList(),
                    variableLookup = variableManager,
                )
            }
            .plus(getVariablesInUseInVariables(variables))
            .toSet()
    }

    private fun getVariablesInUseInVariables(variables: List<Variable>): List<VariableId> =
        variables.flatMap(::getVariablesInUseInVariable)

    private fun getVariablesInUseInVariable(variable: Variable): Set<VariableId> =
        when (variable.type) {
            VariableType.CONSTANT -> variable.value?.let(Variables::extractVariableIds)
            VariableType.SELECT -> {
                variable.getStringListData(SelectType.KEY_VALUES)
                    ?.flatMap { value ->
                        Variables.extractVariableIds(value)
                    }
                    ?.toSet()
            }
            VariableType.TOGGLE -> {
                variable.getStringListData(ToggleType.KEY_VALUES)
                    ?.flatMap { value ->
                        Variables.extractVariableIds(value)
                    }
                    ?.toSet()
            }
            else -> null
        }
            ?: emptySet()
}
