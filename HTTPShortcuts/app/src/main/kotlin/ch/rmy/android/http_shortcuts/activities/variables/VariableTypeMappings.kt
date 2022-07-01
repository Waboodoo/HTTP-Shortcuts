package ch.rmy.android.http_shortcuts.activities.variables

import androidx.annotation.StringRes
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.enums.VariableType

object VariableTypeMappings {

    fun getTypeName(type: VariableType): Int =
        getTypeMapping(type).name

    fun getTypeMapping(type: VariableType): VariableTypeMapping =
        TYPES_MAP[type]!!

    private val TYPES_MAP by lazy {
        TYPES.associateBy { it.type }
    }

    private val TYPES
        get() = setOf(
            VariableTypeMapping(VariableType.CONSTANT, R.string.variable_type_constant, R.string.variable_type_constant_description),
            VariableTypeMapping(VariableType.SELECT, R.string.variable_type_select, R.string.variable_type_select_description),
            VariableTypeMapping(VariableType.TEXT, R.string.variable_type_text, R.string.variable_type_text_description),
            VariableTypeMapping(VariableType.NUMBER, R.string.variable_type_number, R.string.variable_type_number_description),
            VariableTypeMapping(VariableType.SLIDER, R.string.variable_type_slider, R.string.variable_type_slider_description),
            VariableTypeMapping(VariableType.PASSWORD, R.string.variable_type_password, R.string.variable_type_password_description),
            VariableTypeMapping(VariableType.DATE, R.string.variable_type_date, R.string.variable_type_date_description),
            VariableTypeMapping(VariableType.TIME, R.string.variable_type_time, R.string.variable_type_time_description),
            VariableTypeMapping(VariableType.COLOR, R.string.variable_type_color, R.string.variable_type_color_description),
            VariableTypeMapping(VariableType.TOGGLE, R.string.variable_type_toggle, R.string.variable_type_toggle_description),
            VariableTypeMapping(VariableType.CLIPBOARD, R.string.variable_type_clipboard, R.string.variable_type_clipboard_description),
            VariableTypeMapping(VariableType.UUID, R.string.variable_type_uuid, R.string.variable_type_uuid_description),
        )

    class VariableTypeMapping(
        val type: VariableType,
        @StringRes val name: Int,
        @StringRes val description: Int,
    )
}
