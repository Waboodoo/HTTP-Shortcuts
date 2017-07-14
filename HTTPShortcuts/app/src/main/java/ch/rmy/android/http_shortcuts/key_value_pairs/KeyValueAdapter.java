package ch.rmy.android.http_shortcuts.key_value_pairs;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import ch.rmy.android.http_shortcuts.R;

class KeyValueAdapter<T extends KeyValuePair> extends ArrayAdapter<T> {

    KeyValueAdapter(Context context) {
        super(context, R.layout.key_value_item);
    }

    @NonNull
    @Override
    public View getView(int position, View rowView, @NonNull ViewGroup parent) {
        T parameter = getItem(position);

        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.key_value_item, parent, false);
        }

        if (parameter != null) {
            TextView keyView = (TextView) rowView.findViewById(R.id.text_key);
            keyView.setText(parameter.getKey());

            TextView valueView = (TextView) rowView.findViewById(R.id.text_value);
            valueView.setText(parameter.getValue());
        }
        return rowView;
    }

}