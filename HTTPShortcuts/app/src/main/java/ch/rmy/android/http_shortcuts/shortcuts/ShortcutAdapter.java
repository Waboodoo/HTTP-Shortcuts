package ch.rmy.android.http_shortcuts.shortcuts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import ch.rmy.android.http_shortcuts.R;

public class ShortcutAdapter extends ArrayAdapter<Shortcut> {

    public ShortcutAdapter(Context context) {
        super(context, R.layout.list_item);
    }

    @Override
    public View getView(int position, View rowView, ViewGroup parent) {

        Shortcut shortcut = getItem(position);

        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.list_item, parent, false);
        }

        TextView nameView = (TextView) rowView.findViewById(R.id.name);
        nameView.setText(shortcut.getName());

        TextView descriptionView = (TextView) rowView.findViewById(R.id.description);
        descriptionView.setText(shortcut.getDescription());
        descriptionView.setVisibility((shortcut.getDescription() == null || shortcut.getDescription().isEmpty()) ? View.GONE : View.VISIBLE);

        ImageView iconView = (ImageView) rowView.findViewById(R.id.icon);
        iconView.setImageURI(shortcut.getIconURI(getContext()));

        if (shortcut.getIconName() != null && shortcut.getIconName().startsWith("white_")) {
            iconView.setBackgroundColor(0xFF000000);
        } else {
            iconView.setBackgroundColor(0);
        }

        return rowView;
    }

}
