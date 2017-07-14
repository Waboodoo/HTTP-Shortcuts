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

public class ToggleEditorFragment extends VariableEditorFragment {

    @Bind(R.id.toggle_options_list)
    LinearLayout optionsList;
    @Bind(R.id.toggle_options_add_button)
    Button addButton;

    private Variable variable;

    @Override
    protected int getLayoutResource() {
        return R.layout.variable_editor_toggle;
    }

    @Override
    protected void setupViews(View parent) {
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDialog();
            }
        });
    }

    private void showAddDialog() {
        new MaterialDialog.Builder(getContext())
                .title(R.string.title_add_toggle_option)
                .input(null, null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        addNewOption(input.toString());
                    }
                })
                .show();
    }

    private void addNewOption(String value) {
        Option option = Option.createNew(value, value);
        variable.getOptions().add(option);
        updateViews(variable);
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
        View optionView = getLayoutInflater(null).inflate(R.layout.toggle_option, optionsList, false);
        ((TextView) optionView.findViewById(R.id.toggle_option_value)).setText(option.getValue());
        optionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDialog(option, index);
            }
        });
        return optionView;
    }

    private void showEditDialog(final Option option, final int index) {
        new MaterialDialog.Builder(getContext())
                .title(R.string.title_add_toggle_option)
                .input(null, option.getValue(), new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        updateOption(option, input.toString());
                    }
                })
                .neutralText(R.string.dialog_remove)
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        removeOption(index);
                    }
                })
                .show();
    }

    private void updateOption(Option option, String value) {
        option.setValue(value);
        updateViews(variable);
    }

    private void removeOption(int index) {
        variable.getOptions().remove(index);
        updateViews(variable);
    }

    @Override
    public boolean validate() {
        if (variable.getOptions().size() < 2) {
            new MaterialDialog.Builder(getContext())
                    .content(R.string.error_not_enough_toggle_values)
                    .positiveText(R.string.dialog_ok)
                    .show();
            return false;
        }
        return true;
    }

}
