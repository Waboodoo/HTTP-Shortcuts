package ch.rmy.android.http_shortcuts.activities.variables

import androidx.annotation.StringRes
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.enums.VariableType

object VariableTypeMappings {

    fun getTypeName(type: VariableType): Int =
        TYPES.find { it.type == type }!!.name

    val TYPES = listOf(
        VariableTypeMapping(VariableType.CONSTANT, R.string.variable_type_constant),
        VariableTypeMapping(VariableType.SELECT, R.string.variable_type_select),
        VariableTypeMapping(VariableType.TEXT, R.string.variable_type_text),
        VariableTypeMapping(VariableType.NUMBER, R.string.variable_type_number),
        VariableTypeMapping(VariableType.SLIDER, R.string.variable_type_slider),
        VariableTypeMapping(VariableType.PASSWORD, R.string.variable_type_password),
        VariableTypeMapping(VariableType.DATE, R.string.variable_type_date),
        VariableTypeMapping(VariableType.TIME, R.string.variable_type_time),
        VariableTypeMapping(VariableType.COLOR, R.string.variable_type_color),
        VariableTypeMapping(VariableType.TOGGLE, R.string.variable_type_toggle),
    )

    class VariableTypeMapping(val type: VariableType, @StringRes val name: Int)
}
