package ch.rmy.android.http_shortcuts.variables.types;

import ch.rmy.android.http_shortcuts.realm.Controller;
import ch.rmy.android.http_shortcuts.realm.models.Variable;

public class ToggleType extends BaseVariableType implements SyncVariableType {

    @Override
    public String resolveValue(Controller controller, Variable variable) {
        if (variable.getOptions().isEmpty()) {
            return "";
        }
        int previousIndex;
        try {
            previousIndex = Integer.valueOf(variable.getValue());
        } catch (NumberFormatException e) {
            previousIndex = 0;
        }

        int index = previousIndex + 1;
        if (index >= variable.getOptions().size()) {
            index = 0;
        }
        controller.setVariableValue(variable, String.valueOf(index));
        return variable.getOptions().get(index).getValue();
    }

}
