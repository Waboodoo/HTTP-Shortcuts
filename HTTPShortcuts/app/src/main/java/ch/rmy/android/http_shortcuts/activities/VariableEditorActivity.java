package ch.rmy.android.http_shortcuts.activities;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.satsuware.usefulviews.LabelledSpinner;

import butterknife.Bind;
import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.realm.Controller;
import ch.rmy.android.http_shortcuts.realm.models.Variable;
import ch.rmy.android.http_shortcuts.utils.ArrayUtil;
import ch.rmy.android.http_shortcuts.utils.GsonUtil;
import ch.rmy.android.http_shortcuts.utils.OnItemChosenListener;
import ch.rmy.android.http_shortcuts.utils.ShortcutUIUtils;
import ch.rmy.android.http_shortcuts.utils.SimpleTextWatcher;
import ch.rmy.android.http_shortcuts.utils.UIUtil;
import ch.rmy.android.http_shortcuts.variables.Variables;
import ch.rmy.android.http_shortcuts.variables.types.AsyncVariableType;
import ch.rmy.android.http_shortcuts.variables.types.BaseVariableType;
import ch.rmy.android.http_shortcuts.variables.types.TypeFactory;
import ch.rmy.android.http_shortcuts.variables.types.VariableEditorFragment;

public class VariableEditorActivity extends BaseActivity {

    public final static String EXTRA_VARIABLE_ID = "ch.rmy.android.http_shortcuts.activities.VariableEditorActivity.variable_id";
    private static final String STATE_JSON_VARIABLE = "variable_json";

    @Bind(R.id.input_variable_type)
    LabelledSpinner typeSpinner;
    @Bind(R.id.input_variable_key)
    EditText keyView;
    @Bind(R.id.input_variable_title)
    EditText titleView;
    @Bind(R.id.input_url_encode)
    CheckBox urlEncode;
    @Bind(R.id.input_json_encode)
    CheckBox jsonEncode;
    @Bind(R.id.input_allow_share)
    CheckBox allowShare;

    private Controller controller;
    private Variable oldVariable;
    private Variable variable;

    private VariableEditorFragment fragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_variable_editor);

        controller = destroyer.own(new Controller());

        long variableId = getIntent().getLongExtra(EXTRA_VARIABLE_ID, 0);
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_JSON_VARIABLE)) {
            variable = GsonUtil.INSTANCE.fromJson(savedInstanceState.getString(STATE_JSON_VARIABLE), Variable.class);
        } else {
            variable = variableId == 0 ? Variable.Companion.createNew() : controller.getDetachedVariableById(variableId);
        }
        if (variable == null) {
            finish();
            return;
        }
        oldVariable = variableId == 0 ? Variable.Companion.createNew() : controller.getDetachedVariableById(variableId);

        initViews();
        initTypeSelector();
    }

    private void initViews() {
        keyView.setText(variable.getKey());
        titleView.setText(variable.getTitle());
        final ColorStateList defaultColor = keyView.getTextColors();
        keyView.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (Variables.INSTANCE.isValidVariableName(s.toString())) {
                    keyView.setTextColor(defaultColor);
                    keyView.setError(null);
                } else {
                    keyView.setTextColor(Color.RED);
                    keyView.setError(s.length() == 0 ? null : getString(R.string.warning_invalid_variable_key));
                }
            }
        });

        typeSpinner.setItemsArray(ShortcutUIUtils.INSTANCE.getVariableTypeOptions(getContext()));
        UIUtil.INSTANCE.fixLabelledSpinner(typeSpinner);
        typeSpinner.setSelection(ArrayUtil.INSTANCE.findIndex(Variable.Companion.getTYPE_OPTIONS(), variable.getType()));

        urlEncode.setChecked(variable.getUrlEncode());
        jsonEncode.setChecked(variable.getJsonEncode());
        allowShare.setChecked(variable.isShareText());

        setTitle(variable.isNew() ? R.string.create_variable : R.string.edit_variable);

        updateTypeEditor();
    }

    private void initTypeSelector() {
        typeSpinner.setOnItemChosenListener(new OnItemChosenListener() {
            @Override
            public void onSelectionChanged() {
                updateTypeEditor();
            }
        });
    }

    private void updateTypeEditor() {
        compileVariable();
        BaseVariableType variableType = TypeFactory.INSTANCE.getType(getSelectedType());
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragment = variableType.getEditorFragment(fragmentManager);

        titleView.setVisibility(variableType instanceof AsyncVariableType && ((AsyncVariableType) variableType).hasTitle() ? View.VISIBLE : View.GONE);

        fragmentManager
                .beginTransaction()
                .replace(R.id.variable_type_fragment_container, fragment, variableType.getTag())
                .commit();
    }

    public void onFragmentStarted() {
        fragment.updateViews(variable);
    }

    private String getSelectedType() {
        return Variable.Companion.getTYPE_OPTIONS()[typeSpinner.getSpinner().getSelectedItemPosition()];
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

    @Override
    public void onBackPressed() {
        confirmClose();
    }

    private void confirmClose() {
        compileVariable();
        if (hasChanges()) {
            (new MaterialDialog.Builder(this))
                    .content(R.string.confirm_discard_changes_message)
                    .positiveText(R.string.dialog_discard)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            finish();
                        }
                    })
                    .negativeText(R.string.dialog_cancel)
                    .show();
        } else {
            finish();
        }
    }

    private boolean hasChanges() {
        return !oldVariable.equals(variable);
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
        variable.setKey(keyView.getText().toString().trim());
        variable.setType(Variable.Companion.getTYPE_OPTIONS()[typeSpinner.getSpinner().getSelectedItemPosition()]);
        variable.setUrlEncode(urlEncode.isChecked());
        variable.setJsonEncode(jsonEncode.isChecked());
        variable.setShareText(allowShare.isChecked());

        if (fragment != null) {
            fragment.compileIntoVariable(variable);
        }
    }

    private boolean validate() {
        if (variable.getKey().isEmpty()) {
            keyView.setError(getString(R.string.validation_key_non_empty));
            UIUtil.INSTANCE.focus(keyView);
            return false;
        }
        Variable otherVariable = controller.getVariableByKey(variable.getKey());
        if (otherVariable != null && otherVariable.getId() != variable.getId()) {
            keyView.setError(getString(R.string.validation_key_already_exists));
            UIUtil.INSTANCE.focus(keyView);
            return false;
        }
        return fragment == null || fragment.validate();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        compileVariable();
        outState.putString(STATE_JSON_VARIABLE, GsonUtil.INSTANCE.toJson(variable));
    }

}
