package ch.rmy.android.http_shortcuts.variables.types;

public class ConstantType extends BaseVariableType {

    @Override
    protected ConstantEditorFragment createEditorFragment() {
        return new ConstantEditorFragment();
    }
}
