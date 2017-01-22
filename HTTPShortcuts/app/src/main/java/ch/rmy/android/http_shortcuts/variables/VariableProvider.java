package ch.rmy.android.http_shortcuts.variables;

import java.util.ArrayList;
import java.util.List;

import ch.rmy.android.http_shortcuts.realm.Controller;
import ch.rmy.android.http_shortcuts.realm.models.Variable;

public class VariableProvider {

    public static final String INTERNAL_VARIABLE_SHARE = "share";

    private static final String[] INTERNAL_VARIABLES = {
            INTERNAL_VARIABLE_SHARE
    };

    public static List<Variable> getVariables(Controller controller) {
        List<Variable> variables = new ArrayList<>();
        variables.addAll(controller.getVariables());

        // Internal variables
        variables.add(createShareVariable());

        return variables;
    }

    private static Variable createShareVariable() {
        Variable variable = new Variable();
        variable.setKey(INTERNAL_VARIABLE_SHARE);
        variable.setType(Variable.TYPE_TEXT);
        return variable;
    }

    public static boolean isInternalVariable(String key) {
        for (String k : INTERNAL_VARIABLES) {
            if (k.equals(key)) {
                return true;
            }
        }
        return false;
    }
}
