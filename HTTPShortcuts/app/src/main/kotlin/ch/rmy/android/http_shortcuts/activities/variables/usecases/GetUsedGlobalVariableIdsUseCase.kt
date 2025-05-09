package ch.rmy.android.http_shortcuts.activities.variables.usecases

import ch.rmy.android.http_shortcuts.data.domains.request_headers.RequestHeaderRepository
import ch.rmy.android.http_shortcuts.data.domains.request_parameters.RequestParameterRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.GlobalVariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.GlobalVariableRepository
import ch.rmy.android.http_shortcuts.data.enums.VariableType
import ch.rmy.android.http_shortcuts.data.models.GlobalVariable
import ch.rmy.android.http_shortcuts.data.models.RequestHeader
import ch.rmy.android.http_shortcuts.data.models.RequestParameter
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.getRequestParametersForShortcuts
import ch.rmy.android.http_shortcuts.extensions.ids
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.VariableResolver
import ch.rmy.android.http_shortcuts.variables.Variables
import ch.rmy.android.http_shortcuts.variables.types.SelectType
import ch.rmy.android.http_shortcuts.variables.types.ToggleType
import javax.inject.Inject

class GetUsedGlobalVariableIdsUseCase
@Inject
constructor(
    private val shortcutRepository: ShortcutRepository,
    private val requestHeaderRepository: RequestHeaderRepository,
    private val requestParameterRepository: RequestParameterRepository,
    private val globalVariableRepository: GlobalVariableRepository,
) {

    suspend operator fun invoke(shortcutId: ShortcutId?) =
        invoke(shortcutId?.let(::setOf))

    suspend operator fun invoke(shortcutIds: Collection<ShortcutId>? = null): Set<GlobalVariableId> {
        val shortcuts = if (shortcutIds != null) {
            shortcutRepository.getShortcutsByIds(shortcutIds)
        } else {
            shortcutRepository.getShortcuts()
        }
        return determineVariablesInUse(
            variables = globalVariableRepository.getGlobalVariables(),
            shortcuts = shortcuts,
            headersByShortcutId = requestHeaderRepository.getRequestHeadersByShortcutIds(shortcuts.ids()),
            parametersByShortcutId = requestParameterRepository.getRequestParametersForShortcuts(shortcuts),
        )
    }

    private fun determineVariablesInUse(
        variables: List<GlobalVariable>,
        shortcuts: List<Shortcut>,
        headersByShortcutId: Map<ShortcutId, List<RequestHeader>>,
        parametersByShortcutId: Map<ShortcutId, List<RequestParameter>>,
    ): Set<GlobalVariableId> {
        val variableManager = VariableManager(variables)
        return shortcuts
            .flatMap { shortcut ->
                VariableResolver.extractGlobalVariableIdsIncludingScripting(
                    shortcut = shortcut,
                    headers = headersByShortcutId[shortcut.id] ?: emptyList(),
                    parameters = parametersByShortcutId[shortcut.id] ?: emptyList(),
                    variableManager = variableManager,
                )
            }
            .plus(getVariablesInUseInVariables(variables))
            .toSet()
    }

    private fun getVariablesInUseInVariables(variables: List<GlobalVariable>): List<GlobalVariableId> =
        variables.flatMap(::getVariablesInUseInVariable)

    private fun getVariablesInUseInVariable(variable: GlobalVariable): Set<GlobalVariableId> =
        when (variable.type) {
            VariableType.CONSTANT -> variable.value?.let(Variables::extractGlobalVariableIds)
            VariableType.SELECT -> {
                variable.getStringListData(SelectType.KEY_VALUES)
                    ?.flatMap { value ->
                        Variables.extractGlobalVariableIds(value)
                    }
                    ?.toSet()
            }
            VariableType.TOGGLE -> {
                variable.getStringListData(ToggleType.KEY_VALUES)
                    ?.flatMap { value ->
                        Variables.extractGlobalVariableIds(value)
                    }
                    ?.toSet()
            }
            else -> null
        }
            ?: emptySet()
}
