package ch.rmy.android.http_shortcuts;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import com.satsuware.usefulviews.LabelledSpinner;

import butterknife.Bind;
import ch.rmy.android.http_shortcuts.realm.Controller;
import ch.rmy.android.http_shortcuts.realm.models.Variable;
import ch.rmy.android.http_shortcuts.utils.ArrayUtil;
import ch.rmy.android.http_shortcuts.utils.GsonUtil;
import ch.rmy.android.http_shortcuts.utils.SimpleTextWatcher;
import ch.rmy.android.http_shortcuts.utils.ViewUtil;
import ch.rmy.android.http_shortcuts.variables.Variables;

public class VariableEditorActivity extends BaseActivity {

    public final static String EXTRA_VARIABLE_ID = "ch.rmy.android.http_shortcuts.VariableEditorActivity.variable_id";
    private static final String STATE_JSON_VARIABLE = "variable_json";

    @Bind(R.id.input_variable_type)
    LabelledSpinner typeSpinner;
    @Bind(R.id.input_variable_name)
    EditText nameView;
    @Bind(R.id.input_variable_title)
    EditText titleView;

    private Controller controller;
    private Variable oldVariable;
    private Variable variable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_variable_editor);

        controller = destroyer.own(new Controller(this));

        long variableId = getIntent().getLongExtra(EXTRA_VARIABLE_ID, 0);
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_JSON_VARIABLE)) {
            variable = GsonUtil.fromJson(savedInstanceState.getString(STATE_JSON_VARIABLE), Variable.class);
        } else {
            variable = variableId == 0 ? Variable.createNew() : controller.getDetachedVariableById(variableId);
        }
        if (variable == null) {
            finish();
            return;
        }
        oldVariable = variableId == 0 ? Variable.createNew() : controller.getDetachedVariableById(variableId);

        initViews();
    }

    private void initViews() {
        nameView.setText(variable.getKey());
        titleView.setText(variable.getTitle());
        final ColorStateList defaultColor = nameView.getTextColors();
        nameView.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (Variables.isValidVariableName(s.toString())) {
                    nameView.setTextColor(defaultColor);
                    nameView.setError(null);
                } else {
                    nameView.setTextColor(Color.RED);
                    nameView.setError(s.length() == 0 ? null : getString(R.string.warning_invalid_variable_name));
                }
            }
        });

        typeSpinner.setItemsArray(Variable.getTypeOptions(this));
        ViewUtil.hideErrorLabel(typeSpinner);
        typeSpinner.setSelection(ArrayUtil.findIndex(Variable.TYPE_OPTIONS, variable.getType()));

        setTitle(variable.isNew() ? R.string.create_variable : R.string.edit_variable);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.variable_editor_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected int getNavigateUpIcon() {
        return R.drawable.ic_clear;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                confirmClose();
                return true;
            }
            case R.id.action_save_variable: {
                trySave();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmClose() {
        //TODO: Check for changes and prompt
        finish();
    }

    private void trySave() {
        compileVariable();
        if (validate()) {
            controller.persist(variable);
            finish();
        }
    }

    private void compileVariable() {
        variable.setTitle(titleView.getText().toString().trim());
        variable.setKey(nameView.getText().toString().trim());
        variable.setType(Variable.TYPE_OPTIONS[typeSpinner.getSpinner().getSelectedItemPosition()]);
    }

    private boolean validate() {
        //TODO: Validate inputs and display error messages
        //TODO: Check if the variable key is unique
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        compileVariable();
        outState.putString(STATE_JSON_VARIABLE, GsonUtil.toJson(variable));
    }

}
