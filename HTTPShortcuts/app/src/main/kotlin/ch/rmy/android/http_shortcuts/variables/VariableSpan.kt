package ch.rmy.android.http_shortcuts.variables

import androidx.annotation.ColorInt
import ch.rmy.android.framework.utils.spans.ColoredSpan
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId

class VariableSpan(@ColorInt color: Int, val variableId: VariableId) : ColoredSpan(color)
