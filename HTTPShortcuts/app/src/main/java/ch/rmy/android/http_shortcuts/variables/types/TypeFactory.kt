package ch.rmy.android.http_shortcuts.variables.types

import ch.rmy.android.http_shortcuts.realm.models.Variable

object TypeFactory {

    fun getType(type: String): BaseVariableType {
        when (type) {
            Variable.TYPE_TEXT -> return TextType()
            Variable.TYPE_NUMBER -> return NumberType()
            Variable.TYPE_PASSWORD -> return PasswordType()
            Variable.TYPE_TOGGLE -> return ToggleType()
            Variable.TYPE_SELECT -> return SelectType()
            Variable.TYPE_COLOR -> return ColorType()
            else -> return ConstantType()
        }
    }

}
