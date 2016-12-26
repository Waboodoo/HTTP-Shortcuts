package ch.rmy.android.http_shortcuts;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import butterknife.Bind;
import ch.rmy.android.http_shortcuts.adapters.VariableAdapter;
import ch.rmy.android.http_shortcuts.dialogs.HelpDialogBuilder;
import ch.rmy.android.http_shortcuts.listeners.OnItemClickedListener;
import ch.rmy.android.http_shortcuts.realm.Controller;
import ch.rmy.android.http_shortcuts.realm.models.Variable;
import ch.rmy.android.http_shortcuts.utils.MenuDialogBuilder;

public class VariablesActivity extends BaseActivity {

    @Bind(R.id.variable_list)
    RecyclerView variableList;
    @Bind(R.id.button_create_variable)
    FloatingActionButton createButton;

    private Controller controller;
    private VariableAdapter adapter;

    private OnItemClickedListener<Variable> clickedListener = new OnItemClickedListener<Variable>() {
        @Override
        public void onItemClicked(Variable variable) {
            editVariable(variable);
        }

        @Override
        public void onItemLongClicked(Variable variable) {
            showContextMenu(variable);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_variables);

        controller = destroyer.own(new Controller(this));

        adapter = destroyer.own(new VariableAdapter(this));
        adapter.setParent(controller.getBase());

        LinearLayoutManager manager = new LinearLayoutManager(this);
        variableList.setLayoutManager(manager);
        variableList.setHasFixedSize(true);
        variableList.addItemDecoration(new ShortcutListDecorator(this, R.drawable.list_divider));
        variableList.setAdapter(adapter);

        adapter.setOnItemClickListener(clickedListener);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditorForCreation();
            }
        });
    }

    private void openEditorForCreation() {
        Intent intent = new Intent(this, VariableEditorActivity.class);
        startActivity(intent);
    }

    private void editVariable(Variable variable) {
        Intent intent = new Intent(this, VariableEditorActivity.class);
        intent.putExtra(VariableEditorActivity.EXTRA_VARIABLE_ID, variable.getId());
        startActivity(intent);
    }

    private void showContextMenu(final Variable variable) {
        MenuDialogBuilder builder = new MenuDialogBuilder(this)
                .title(variable.getKey());

        builder.item(R.string.action_edit, new MenuDialogBuilder.Action() {
            @Override
            public void execute() {
                editVariable(variable);
            }
        });
        builder.item(R.string.action_delete, new MenuDialogBuilder.Action() {
            @Override
            public void execute() {
                showDeleteDialog(variable);
            }
        });
        builder.show();
    }

    private void showDeleteDialog(final Variable variable) {
        (new MaterialDialog.Builder(this))
                .content(R.string.confirm_delete_variable_message)
                .positiveText(R.string.dialog_delete)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        deleteVariable(variable);
                    }
                })
                .negativeText(R.string.dialog_cancel)
                .show();
    }

    private void deleteVariable(Variable variable) {
        showSnackbar(String.format(getString(R.string.variable_deleted), variable.getKey()));
        controller.deleteVariable(variable);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.variables_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_show_help) {
            showHelp();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showHelp() {
        destroyer.own(new HelpDialogBuilder(this)
                .title(R.string.help_title_variables)
                .message(R.string.help_variables)
                .build()
        ).show();
    }

}
