package ch.rmy.android.http_shortcuts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import ch.rmy.android.http_shortcuts.shortcuts.ShortcutStorage;

public class SettingsActivity extends Activity {

	private static final String SHORTCUT_DATABASE_FILE_NAME = "shortcuts";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SettingsFragment settingsFragment = new SettingsFragment();
		getFragmentManager().beginTransaction().replace(android.R.id.content, settingsFragment).commit();
	}

	public static class SettingsFragment extends PreferenceFragment {

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			addPreferencesFromResource(R.xml.preferences);

			final ListPreference clickBehaviorPreference = (ListPreference) findPreference("click_behavior");
			clickBehaviorPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					clickBehaviorPreference.setSummary(clickBehaviorPreference.getEntries()[clickBehaviorPreference.findIndexOfValue((String) newValue)]);
					return true;
				}

			});
			clickBehaviorPreference.setSummary(clickBehaviorPreference.getEntries()[clickBehaviorPreference.findIndexOfValue(clickBehaviorPreference.getValue())]);

			final Preference exportPreference = findPreference("export");
			exportPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

				public boolean onPreferenceClick(Preference preference) {
					ShortcutStorage database = new ShortcutStorage(getActivity());
					File sourceFile = database.getDatabaseFile();
					File targetFile = null;
					int counter = 0;
					do {
						counter++;
						targetFile = new File(Environment.getExternalStorageDirectory(), SHORTCUT_DATABASE_FILE_NAME + (counter == 1 ? "" : "_" + counter) + ".db");
					} while (targetFile.exists());

					CopyDatabaseTask copyTask = new CopyDatabaseTask(getActivity());
					copyTask.execute(sourceFile, targetFile);

					return true;
				}

			});

		}

	}

	private static class CopyDatabaseTask extends AsyncTask<File, Integer, String> {

		private final Context context;

		protected CopyDatabaseTask(Context context) {
			this.context = context;
		}

		protected String doInBackground(File... files) {
			File sourceFile = files[0];
			File targetFile = files[1];

			boolean success = copyFile(sourceFile, targetFile);
			if (success) {
				return targetFile.getAbsolutePath();
			}

			return null;
		}

		protected void onPostExecute(String path) {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			AlertDialog dialog = builder.create();

			if (path == null) {
				dialog.setTitle(R.string.export_failed_title);
				dialog.setMessage(context.getString(R.string.export_failed_message));
			} else {
				dialog.setTitle(R.string.export_success_title);
				dialog.setMessage(context.getString(R.string.export_success_message, path));
			}
			dialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.button_ok), (OnClickListener) null);
			dialog.setCanceledOnTouchOutside(true);

			dialog.show();
		}

		private boolean copyFile(File sourceFile, File targetFile) {
			InputStream in = null;
			OutputStream out = null;

			try {
				try {
					in = new FileInputStream(sourceFile);
					out = new FileOutputStream(targetFile);

					byte[] buf = new byte[1024];
					int len;
					while ((len = in.read(buf)) > 0) {
						out.write(buf, 0, len);
					}

					return true;
				} finally {
					if (in != null) {
						in.close();
					}
					if (out != null) {
						out.close();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}

	}

}
