package ch.rmy.android.http_shortcuts.shortcuts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import ch.rmy.android.http_shortcuts.R;

public class HeaderAdapter extends ArrayAdapter<Header> {

	public HeaderAdapter(Context context) {
		super(context, R.layout.custom_header_item);
	}

	@Override
	public View getView(int position, View rowView, ViewGroup parent) {
		Header header = getItem(position);

		if (rowView == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.custom_header_item, parent, false);
		}

		TextView keyView = (TextView) rowView.findViewById(R.id.text_key);
		keyView.setText(header.getKey());

		TextView valueView = (TextView) rowView.findViewById(R.id.text_value);
		valueView.setText("= " + header.getValue());
		return rowView;
	}

}