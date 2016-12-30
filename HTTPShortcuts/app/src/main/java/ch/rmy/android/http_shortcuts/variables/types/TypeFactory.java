package ch.rmy.android.http_shortcuts.variables.types;

import ch.rmy.android.http_shortcuts.realm.models.Variable;

public class TypeFactory {

    public static BaseVariableType getType(String type) {
        switch (type) {
            case Variable.TYPE_TEXT:
                return new TextType();
            case Variable.TYPE_NUMBER:
                return new NumberType();
            case Variable.TYPE_PASSWORD:
                return new PasswordType();
            case Variable.TYPE_TOGGLE:
                return new ToggleType();
            case Variable.TYPE_SELECT:
                return new SelectType();
            case Variable.TYPE_COLOR:
                return new ColorType();
            case Variable.TYPE_CONSTANT:
            default:
                return new ConstantType();
        }
    }

}
