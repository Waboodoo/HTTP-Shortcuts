package ch.rmy.android.http_shortcuts.activities.execute.usecases

import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKeyOrId
import javax.inject.Inject

class ExtractFileIdsFromVariableValuesUseCase
@Inject
constructor() {
    operator fun invoke(variableValues: Map<VariableKeyOrId, String>) =
        variableValues[VariableKeyOrId("\$files")]
            ?.trim('[', ']')
            ?.split(",")
            ?.map { it.trim(' ', '"') }
            ?: emptyList()
}
