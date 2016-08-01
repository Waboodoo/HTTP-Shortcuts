package ch.rmy.android.http_shortcuts.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.realm.models.Base;
import ch.rmy.android.http_shortcuts.realm.models.Variable;

public class VariableAdapter extends BaseAdapter<Base, Variable> {

    public VariableAdapter(Context context) {
        super(context);
    }

    @Override
    protected List<Variable> getItems(Base base) {
        return base.getVariables();
    }

    @Override
    protected VariableViewHolder createViewHolder(ViewGroup parentView) {
        return new VariableViewHolder(parentView);
    }

    public class VariableViewHolder extends BaseViewHolder<Variable> {

        @Bind(R.id.name)
        TextView name;

        public VariableViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(context).inflate(R.layout.list_item_variable, parent, false), VariableAdapter.this);
            ButterKnife.bind(this, itemView);
        }

        @Override
        protected void updateViews(Variable variable) {
            name.setText(variable.getKey());
        }

    }
}
