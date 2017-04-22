package ch.rmy.android.http_shortcuts.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.realm.models.Variable;
import ch.rmy.android.http_shortcuts.utils.ArrayUtil;

public class VariableAdapter extends BaseAdapter<Variable> {

    public VariableAdapter(Context context) {
        super(context);
    }

    @Override
    protected VariableViewHolder createViewHolder(ViewGroup parentView) {
        return new VariableViewHolder(parentView);
    }

    @Override
    protected int getEmptyMarkerStringResource() {
        return R.string.no_variables;
    }

    public class VariableViewHolder extends BaseViewHolder<Variable> {

        @Bind(R.id.name)
        TextView name;
        @Bind(R.id.type)
        TextView type;

        public VariableViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(context).inflate(R.layout.list_item_variable, parent, false), VariableAdapter.this);
            ButterKnife.bind(this, itemView);
        }

        @Override
        protected void updateViews(Variable variable) {
            name.setText(variable.getKey());
            type.setText(Variable.TYPE_RESOURCES[ArrayUtil.findIndex(Variable.TYPE_OPTIONS, variable.getType())]);
        }

    }
}
