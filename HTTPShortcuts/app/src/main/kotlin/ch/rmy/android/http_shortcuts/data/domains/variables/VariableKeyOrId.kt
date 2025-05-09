package ch.rmy.android.http_shortcuts.data.domains.variables

import ch.rmy.android.http_shortcuts.variables.Variables

@JvmInline
value class VariableKeyOrId(val value: String) {
    val globalVariableId: GlobalVariableId?
        get() = value.takeIf { Variables.isValidGlobalVariableId(value) }
    val variableKey: VariableKey?
        get() = value.takeIf { Variables.isValidVariableKey(it) }

    override fun toString(): String =
        value
}
