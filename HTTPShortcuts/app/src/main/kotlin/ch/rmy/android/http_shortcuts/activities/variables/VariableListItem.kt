package ch.rmy.android.http_shortcuts.activities.variables

import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.http_shortcuts.R

sealed interface VariableListItem {
    data class Variable(
        val id: String,
        val key: String,
        val type: Localizable,
        val isUnused: Boolean,
    ) : VariableListItem

    object EmptyState : VariableListItem {
        val title: Localizable
            get() = StringResLocalizable(R.string.empty_state_variables)

        val instructions: Localizable
            get() = StringResLocalizable(R.string.empty_state_variables_instructions)
    }
}
