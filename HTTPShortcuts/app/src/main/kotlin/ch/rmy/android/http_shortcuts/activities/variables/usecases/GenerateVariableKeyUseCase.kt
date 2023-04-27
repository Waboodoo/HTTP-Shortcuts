package ch.rmy.android.http_shortcuts.activities.variables.usecases

import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.variables.Variables
import javax.inject.Inject

class GenerateVariableKeyUseCase
@Inject
constructor() {
    operator fun invoke(baseKey: VariableKey, existingVariableKeys: List<VariableKey>): VariableKey {
        val base = baseKey.take(Variables.KEY_MAX_LENGTH - 2)
        for (i in 2..99) {
            val newKey = "$base$i"
            if (newKey !in existingVariableKeys) {
                return newKey
            }
        }
        return generateRandomString()
    }

    private fun generateRandomString() =
        List(Variables.KEY_MAX_LENGTH - 2) {
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".random()
        }
            .joinToString(separator = "")
}
