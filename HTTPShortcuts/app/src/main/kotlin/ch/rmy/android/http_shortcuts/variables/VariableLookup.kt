package ch.rmy.android.http_shortcuts.variables

import ch.rmy.android.http_shortcuts.data.models.VariableModel

interface VariableLookup {

    fun getVariableById(id: String): VariableModel?

    fun getVariableByKey(key: String): VariableModel?
}
