package ch.rmy.android.http_shortcuts.key_value_pairs;

import android.app.Dialog;
import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.realm.Controller;
import ch.rmy.android.http_shortcuts.utils.Destroyable;
import ch.rmy.android.http_shortcuts.utils.Destroyer;
import ch.rmy.android.http_shortcuts.utils.VariableFormatter;

public class KeyValueList<T extends KeyValuePair> extends FrameLayout implements Destroyable {

    @Bind(R.id.key_value_list)
    ListView listView;
    @Bind(R.id.key_value_list_button)
    Button button;

    private KeyValueAdapter<T> adapter;
    private KeyValuePairFactory<T> factory;
    private int addDialogTitle;
    private int editDialogTitle;
    private int keyLabel;
    private int valueLabel;
    private ArrayAdapter<String> suggestionAdapter;

    private Controller controller;

    private final Destroyer destroyer = new Destroyer();

    public KeyValueList(Context context) {
        super(context);
        init();
    }

    public KeyValueList(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public KeyValueList(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.key_value_list, this);
        ButterKnife.bind(this, this);

        controller = destroyer.own(new Controller(getContext()));

        adapter = new KeyValueAdapter<>(getContext());
        listView.setAdapter(adapter);

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDialog();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                T item = adapter.getItem(position);
                showEditDialog(item);
            }
        });
    }

    private void showAddDialog() {
        showDialog(null);
    }

    private void showEditDialog(final T item) {
        showDialog(item);
    }

    private void showDialog(final T item) {
        final boolean isNew = item == null;

        MaterialDialog.Builder builder = new MaterialDialog.Builder(getContext())
                .customView(R.layout.dialog_key_value_editor, false)
                .title(isNew ? addDialogTitle : editDialogTitle)
                .positiveText(R.string.dialog_ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        EditText keyField = (EditText) dialog.findViewById(R.id.key_value_key);
                        EditText valueField = (EditText) dialog.findViewById(R.id.key_value_value);
                        if (!keyField.getText().toString().isEmpty()) {
                            if (isNew) {
                                T newItem = factory.create(keyField.getText().toString(), valueField.getText().toString());
                                adapter.add(newItem);
                                updateListViewHeightBasedOnChildren();
                            } else {
                                item.setKey(keyField.getText().toString());
                                item.setValue(valueField.getText().toString());
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
        if (!isNew) {
            builder.neutralText(R.string.dialog_remove)
                    .onNeutral(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(MaterialDialog dialog, DialogAction which) {
                            adapter.remove(item);
                            updateListViewHeightBasedOnChildren();
                        }
                    });
        }

        builder.negativeText(R.string.dialog_cancel);

        Dialog dialog = builder.build();

        AutoCompleteTextView keyInput = (AutoCompleteTextView) dialog.findViewById(R.id.key_value_key);
        EditText valueInput = (EditText) dialog.findViewById(R.id.key_value_value);
        if (!isNew) {
            keyInput.setText(item.getKey());
            valueInput.setText(item.getValue());
        }
        destroyer.own(VariableFormatter.bind(keyInput, controller.getVariables()));
        destroyer.own(VariableFormatter.bind(valueInput, controller.getVariables()));

        ((TextInputLayout) dialog.findViewById(R.id.key_value_key_layout)).setHint(getContext().getString(keyLabel));
        ((TextInputLayout) dialog.findViewById(R.id.key_value_value_layout)).setHint(getContext().getString(valueLabel));

        if (suggestionAdapter != null) {
            keyInput.setAdapter(suggestionAdapter);
        }

        dialog.show();
    }

    public void addItems(Collection<T> items) {
        adapter.addAll(items);
        updateListViewHeightBasedOnChildren();
    }

    public List<T> getItems() {
        List<T> list = new ArrayList<T>();
        for (int i = 0; i < adapter.getCount(); i++) {
            list.add(adapter.getItem(i));
        }
        return list;
    }

    public void setButtonText(int resId) {
        button.setText(resId);
    }

    public void setAddDialogTitle(int resId) {
        this.addDialogTitle = resId;
    }

    public void setEditDialogTitle(int resId) {
        this.editDialogTitle = resId;
    }

    public void setItemFactory(KeyValuePairFactory<T> factory) {
        this.factory = factory;
    }

    public void setKeyLabel(int resId) {
        this.keyLabel = resId;
    }

    public void setValueLabel(int resId) {
        this.valueLabel = resId;
    }

    public void setSuggestions(String[] suggestions) {
        this.suggestionAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, suggestions);
    }

    private void updateListViewHeightBasedOnChildren() {
        int desiredWidth = MeasureSpec.makeMeasureSpec(listView.getWidth(), MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < adapter.getCount(); i++) {
            view = adapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    @Override
    public void destroy() {
        destroyer.destroy();
    }
}
