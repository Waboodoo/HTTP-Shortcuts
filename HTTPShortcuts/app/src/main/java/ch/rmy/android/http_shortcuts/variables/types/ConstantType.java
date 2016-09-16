package ch.rmy.android.http_shortcuts.variables.types;

import ch.rmy.android.http_shortcuts.realm.Controller;
import ch.rmy.android.http_shortcuts.realm.models.Variable;

public class ConstantType extends BaseVariableType implements SyncVariableType {

    @Override
    protected ConstantEditorFragment createEditorFragment() {
        return new ConstantEditorFragment();
    }

    @Override
    public String resolveValue(Controller controller, Variable variable) {
        return variable.getValue();
    }

}
