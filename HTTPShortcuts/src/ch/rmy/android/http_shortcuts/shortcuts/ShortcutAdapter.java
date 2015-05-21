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

		TextView urlView = (TextView) rowView.findViewById(R.id.url);
		String subText = shortcut.getMethod() + " " + (shortcut.getProtocol().equals(Shortcut.PROTOCOL_HTTPS) ? "https://" : "") + shortcut.getURL();
		if (subText.length() > 100) {
			subText = subText.substring(0, 100) + "...";
		}
		urlView.setText(subText);

		ImageView iconView = (ImageView) rowView.findViewById(R.id.icon);
		iconView.setImageURI(shortcut.getIconURI(getContext()));

		return rowView;
	}

}
