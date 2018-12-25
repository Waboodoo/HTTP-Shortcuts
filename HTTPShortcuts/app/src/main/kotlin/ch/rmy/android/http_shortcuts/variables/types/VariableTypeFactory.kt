package ch.rmy.android.http_shortcuts.variables.types

import ch.rmy.android.http_shortcuts.realm.models.Variable

object VariableTypeFactory {

    fun getType(type: String): BaseVariableType = when (type) {
        Variable.TYPE_TEXT -> TextType()
        Variable.TYPE_NUMBER -> NumberType()
        Variable.TYPE_PASSWORD -> PasswordType()
        Variable.TYPE_TOGGLE -> ToggleType()
        Variable.TYPE_SELECT -> SelectType()
        Variable.TYPE_COLOR -> ColorType()
        Variable.TYPE_DATE -> DateType()
        Variable.TYPE_TIME -> TimeType()
        Variable.TYPE_SLIDER -> SliderType()
        else -> ConstantType()
    }

}
