package ch.rmy.android.http_shortcuts.activities.execute.usecases

import javax.inject.Inject

class ExtractFileIdsFromVariableValuesUseCase
@Inject
constructor() {
    operator fun invoke(variableValues: Map<String, String>) =
        variableValues["\$files"]
            ?.trim('[', ']')
            ?.split(",")
            ?.map { it.trim(' ', '"') }
            ?: emptyList()
}
