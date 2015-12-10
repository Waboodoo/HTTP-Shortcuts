package ch.rmy.android.http_shortcuts;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class ChangeLogDialog {

	private static final String PREFERENCES_NAME = "changelog";
	private static final String KEY_PERMANENTLY_HIDDEN = "permanenty_hidden";
	private static final String KEY_LAST_VERSION = "last_version";

	private final Context context;
	private final SharedPreferences preferences;
	private final boolean whatsNew;

	public ChangeLogDialog(Context context, boolean whatsNew) {
		this.context = context;
		this.whatsNew = whatsNew;

		preferences = context.getSharedPreferences(PREFERENCES_NAME, 0);
	}

	public boolean isPermanentlyHidden() {
		return preferences.getBoolean(KEY_PERMANENTLY_HIDDEN, false);
	}

	public boolean wasAlreadyShown() {
		return getVersion() == preferences.getInt(KEY_LAST_VERSION, 0);
	}

	public void show() {
		Editor editor = preferences.edit();
		editor.putInt(KEY_LAST_VERSION, getVersion());
		editor.commit();

		LayoutInflater layoutInflater = LayoutInflater.from(context);
		View view = layoutInflater.inflate(R.layout.changelog_dialog, null);

		new AlertDialog.Builder(context).setView(view).setTitle(whatsNew ? R.string.changelog_title_whats_new : R.string.changelog_title)
				.setIcon(android.R.drawable.ic_menu_info_details).setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

					}
				}).show();

		TextView text = (TextView) view.findViewById(R.id.changelog_text);
		text.setText(Html.fromHtml(context.getString(R.string.changelog_text)));

		CheckBox showAtStartupCheckbox = (CheckBox) view.findViewById(R.id.checkbox_show_at_startup);
		showAtStartupCheckbox.setChecked(!isPermanentlyHidden());
		showAtStartupCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Editor editor = preferences.edit();
				editor.putBoolean(KEY_PERMANENTLY_HIDDEN, !isChecked);
				editor.commit();
			}

		});

	}

	private int getVersion() {
		try {
			return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			return 0;
		}
	}

}
