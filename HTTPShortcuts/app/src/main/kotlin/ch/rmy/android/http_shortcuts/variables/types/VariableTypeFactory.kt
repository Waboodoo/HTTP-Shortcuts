package ch.rmy.android.http_shortcuts.variables.types

import ch.rmy.android.http_shortcuts.data.enums.VariableType

object VariableTypeFactory {

    fun getType(type: VariableType): BaseVariableType = when (type) {
        VariableType.CONSTANT -> ConstantType()
        VariableType.TEXT -> TextType()
        VariableType.NUMBER -> NumberType()
        VariableType.PASSWORD -> PasswordType()
        VariableType.TOGGLE -> ToggleType()
        VariableType.SELECT -> SelectType()
        VariableType.COLOR -> ColorType()
        VariableType.DATE -> DateType()
        VariableType.TIME -> TimeType()
        VariableType.SLIDER -> SliderType()
    }
}
