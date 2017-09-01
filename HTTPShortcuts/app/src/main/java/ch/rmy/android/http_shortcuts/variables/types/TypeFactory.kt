package ch.rmy.android.http_shortcuts.variables.types

import ch.rmy.android.http_shortcuts.realm.models.Variable

object TypeFactory {

    fun getType(type: String): BaseVariableType {
        return when (type) {
            Variable.TYPE_TEXT -> TextType()
            Variable.TYPE_NUMBER -> NumberType()
            Variable.TYPE_PASSWORD -> PasswordType()
            Variable.TYPE_TOGGLE -> ToggleType()
            Variable.TYPE_SELECT -> SelectType()
            Variable.TYPE_COLOR -> ColorType()
            Variable.TYPE_DATE -> DateType()
            else -> ConstantType()
        }
    }

}
