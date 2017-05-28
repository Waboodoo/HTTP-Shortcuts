package ch.rmy.android.http_shortcuts.variables.types;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import butterknife.Bind;
import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.realm.models.Option;
import ch.rmy.android.http_shortcuts.realm.models.Variable;

public class SelectEditorFragment extends VariableEditorFragment {

    @Bind(R.id.select_options_list)
    LinearLayout optionsList;
    @Bind(R.id.select_options_add_button)
    Button addButton;

    private Variable variable;

    @Override
    protected int getLayoutResource() {
        return R.layout.variable_editor_select;
    }

    @Override
    protected void setupViews(View parent) {
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDialog(null, -1);
            }
        });
    }

    @Override
    public void updateViews(Variable variable) {
        this.variable = variable;
        optionsList.removeAllViews();
        int i = 0;
        for (Option option : variable.getOptions()) {
            optionsList.addView(createOptionView(option, i));
            i++;
        }
    }

    private View createOptionView(final Option option, final int index) {
        View optionView = getLayoutInflater(null).inflate(R.layout.select_option, optionsList, false);
        ((TextView) optionView.findViewById(R.id.select_option_label)).setText(option.getLabel());
        optionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDialog(option, index);
            }
        });
        return optionView;
    }

    private void showEditDialog(final Option option, final int index) {
        View editorView = getLayoutInflater(null).inflate(R.layout.select_option_editor_item, null);
        final TextView labelInput = (TextView) editorView.findViewById(R.id.select_option_label);
        final TextView valueInput = (TextView) editorView.findViewById(R.id.select_option_value);

        if (option != null) {
            labelInput.setText(option.getLabel());
            valueInput.setText(option.getValue());
        }
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getContext())
                .title(R.string.title_add_select_option)
                .customView(editorView, true)
                .positiveText(R.string.dialog_ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        String label = labelInput.getText().toString();
                        String value = valueInput.getText().toString();
                        if (option != null) {
                            updateOption(option, label, value);
                        } else {
                            addNewOption(label, value);
                        }
                    }
                })
                .negativeText(R.string.dialog_cancel);

        if (option != null) {
            builder = builder
                    .neutralText(R.string.dialog_remove)
                    .onNeutral(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(MaterialDialog dialog, DialogAction which) {
                            removeOption(index);
                        }
                    });
        }

        builder.show();
    }

    private void addNewOption(String label, String value) {
        Option option = Option.createNew(label, value);
        variable.getOptions().add(option);
        updateViews(variable);
    }

    private void updateOption(Option option, String label, String value) {
        option.setLabel(label);
        option.setValue(value);
        updateViews(variable);
    }

    private void removeOption(int index) {
        variable.getOptions().remove(index);
        updateViews(variable);
    }

    @Override
    public boolean validate() {
        if (variable.getOptions().isEmpty()) {
            new MaterialDialog.Builder(getContext())
                    .content(R.string.error_not_enough_select_values)
                    .positiveText(R.string.dialog_ok)
                    .show();
            return false;
        }
        return true;
    }

}
