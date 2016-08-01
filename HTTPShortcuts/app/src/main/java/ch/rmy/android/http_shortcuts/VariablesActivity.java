package ch.rmy.android.http_shortcuts;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

import butterknife.Bind;
import ch.rmy.android.http_shortcuts.adapters.VariableAdapter;
import ch.rmy.android.http_shortcuts.listeners.OnItemClickedListener;
import ch.rmy.android.http_shortcuts.realm.Controller;
import ch.rmy.android.http_shortcuts.realm.models.Variable;

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

        }

        @Override
        public void onItemLongClicked(Variable variable) {

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
                openCreateDialog();
            }
        });
    }

    private void openCreateDialog() {

    }

}
