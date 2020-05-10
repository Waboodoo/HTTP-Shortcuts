package ch.rmy.android.http_shortcuts.activities.variables

import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.models.Variable

object VariableTypes {

    fun getTypeName(type: String): Int =
        TYPES.find { it.first == type }!!.second

    val TYPES = listOf(
        Variable.TYPE_CONSTANT to R.string.variable_type_constant,
        Variable.TYPE_SELECT to R.string.variable_type_select,
        Variable.TYPE_TEXT to R.string.variable_type_text,
        Variable.TYPE_NUMBER to R.string.variable_type_number,
        Variable.TYPE_SLIDER to R.string.variable_type_slider,
        Variable.TYPE_PASSWORD to R.string.variable_type_password,
        Variable.TYPE_DATE to R.string.variable_type_date,
        Variable.TYPE_TIME to R.string.variable_type_time,
        Variable.TYPE_COLOR to R.string.variable_type_color,
        Variable.TYPE_TOGGLE to R.string.variable_type_toggle
    )

}