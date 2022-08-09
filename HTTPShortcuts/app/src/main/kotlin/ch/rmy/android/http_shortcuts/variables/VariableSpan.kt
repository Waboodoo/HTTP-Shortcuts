package ch.rmy.android.http_shortcuts.variables

import androidx.annotation.ColorInt
import ch.rmy.android.framework.utils.spans.ColoredSpan
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.utils.LengthAwareSpan

class VariableSpan(
    @ColorInt color: Int,
    val variableId: VariableId,
    override val length: Int,
) : ColoredSpan(color), LengthAwareSpan
