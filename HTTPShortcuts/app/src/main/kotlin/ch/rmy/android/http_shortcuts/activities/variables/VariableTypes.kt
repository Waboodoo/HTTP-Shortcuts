package ch.rmy.android.http_shortcuts.activities.variables

import androidx.annotation.StringRes
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.models.Variable

object VariableTypes {

    fun getTypeName(type: String): Int =
        TYPES.find { it.type == type }!!.name

    val TYPES = listOf(
        VariableType(Variable.TYPE_CONSTANT, R.string.variable_type_constant),
        VariableType(Variable.TYPE_SELECT, R.string.variable_type_select),
        VariableType(Variable.TYPE_TEXT, R.string.variable_type_text),
        VariableType(Variable.TYPE_NUMBER, R.string.variable_type_number),
        VariableType(Variable.TYPE_SLIDER, R.string.variable_type_slider),
        VariableType(Variable.TYPE_PASSWORD, R.string.variable_type_password),
        VariableType(Variable.TYPE_DATE, R.string.variable_type_date),
        VariableType(Variable.TYPE_TIME, R.string.variable_type_time),
        VariableType(Variable.TYPE_COLOR, R.string.variable_type_color),
        VariableType(Variable.TYPE_TOGGLE, R.string.variable_type_toggle)
    )

    class VariableType(val type: String, @StringRes val name: Int)
}
