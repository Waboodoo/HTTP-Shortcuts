package ch.rmy.android.http_shortcuts.activities.variables.models

import androidx.compose.runtime.Stable
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.http_shortcuts.data.domains.variables.GlobalVariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey

@Stable
data class GlobalVariableListItem(
    val id: GlobalVariableId,
    val key: VariableKey,
    val type: Localizable,
    val isUnused: Boolean,
)
