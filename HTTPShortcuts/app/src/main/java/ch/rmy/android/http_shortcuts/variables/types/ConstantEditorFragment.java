package ch.rmy.android.http_shortcuts.variables.types;

import android.widget.EditText;

import butterknife.Bind;
import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.realm.models.Variable;

public class ConstantEditorFragment extends VariableEditorFragment {

    @Bind(R.id.input_variable_value)
    EditText value;

    @Override
    protected int getLayoutResource() {
        return R.layout.variable_editor_constant;
    }

    @Override
    public void updateViews(Variable variable) {
        value.setText(variable.getValue());
    }

    @Override
    public void compileIntoVariable(Variable variable) {
        variable.setValue(value.getText().toString());
    }
}
