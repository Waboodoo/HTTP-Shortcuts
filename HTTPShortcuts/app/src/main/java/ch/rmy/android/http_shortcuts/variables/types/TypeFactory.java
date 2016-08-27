package ch.rmy.android.http_shortcuts.variables.types;

import ch.rmy.android.http_shortcuts.realm.models.Variable;

public class TypeFactory {

    public static BaseVariableType getType(String type) {
        switch (type) {
            case Variable.TYPE_CONSTANT:
                return new ConstantType();
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
        }
        throw new IllegalArgumentException("Unknown type: " + type);
    }

}
