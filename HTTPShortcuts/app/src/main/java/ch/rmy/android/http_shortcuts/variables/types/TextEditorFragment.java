package ch.rmy.android.http_shortcuts.variables.types;

import android.widget.CheckBox;

import butterknife.Bind;
import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.realm.models.Variable;

public class TextEditorFragment extends VariableEditorFragment {

    @Bind(R.id.input_remember_value)
    CheckBox rememberValue;

    @Override
    protected int getLayoutResource() {
        return R.layout.variable_editor_text;
    }

    @Override
    public void updateViews(Variable variable) {
        rememberValue.setChecked(variable.getRememberValue());
    }

    @Override
    public void compileIntoVariable(Variable variable) {
        variable.setRememberValue(rememberValue.isChecked());
    }
}
