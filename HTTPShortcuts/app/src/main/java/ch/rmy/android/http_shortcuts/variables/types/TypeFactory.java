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
        }
        throw new IllegalArgumentException("Unknown type: " + type);
    }

}
