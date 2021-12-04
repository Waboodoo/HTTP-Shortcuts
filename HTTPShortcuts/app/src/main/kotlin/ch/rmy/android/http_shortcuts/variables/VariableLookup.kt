package ch.rmy.android.http_shortcuts.variables

import ch.rmy.android.http_shortcuts.data.models.Variable

interface VariableLookup {

    fun getVariableById(id: String): Variable?

    fun getVariableByKey(key: String): Variable?
}
