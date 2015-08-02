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
import android.content.DialogInterface;
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

					ExportDatabaseTask copyTask = new ExportDatabaseTask(getActivity());
					copyTask.execute(sourceFile, targetFile);

					return true;
				}

			});

			final Preference importPreference = findPreference("import");
			importPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

				public boolean onPreferenceClick(Preference preference) {
					ShortcutStorage database = new ShortcutStorage(getActivity());
					final File sourceFile = new File(Environment.getExternalStorageDirectory(), SHORTCUT_DATABASE_FILE_NAME + ".db");
					final File targetFile = database.getDatabaseFile();

					if (sourceFile.exists()) {
						AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
						AlertDialog dialog = builder.create();

						dialog.setTitle(R.string.import_title);
						dialog.setMessage(getActivity().getString(R.string.import_warning_message, sourceFile.getAbsolutePath()));

						dialog.setButton(AlertDialog.BUTTON_POSITIVE, getActivity().getString(R.string.button_ok), new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								ImportDatabaseTask copyTask = new ImportDatabaseTask(getActivity());
								copyTask.execute(sourceFile, targetFile);
							}

						});
						dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getActivity().getString(R.string.button_cancel), (OnClickListener) null);
						dialog.setCanceledOnTouchOutside(true);

						dialog.show();

					} else {
						AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
						AlertDialog dialog = builder.create();

						dialog.setTitle(R.string.import_title);
						dialog.setMessage(getActivity().getString(R.string.import_howto_message, sourceFile.getName(), sourceFile.getParentFile().getAbsolutePath()));

						dialog.setButton(AlertDialog.BUTTON_POSITIVE, getActivity().getString(R.string.button_ok), (OnClickListener) null);
						dialog.setCanceledOnTouchOutside(true);

						dialog.show();
					}

					return true;
				}

			});

		}

	}

	private static class ExportDatabaseTask extends AsyncTask<File, Integer, String> {

		private final Context context;

		protected ExportDatabaseTask(Context context) {
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

	}

	private static class ImportDatabaseTask extends AsyncTask<File, Integer, Integer> {

		private final Context context;

		protected ImportDatabaseTask(Context context) {
			this.context = context;
		}

		protected Integer doInBackground(File... files) {
			File sourceFile = files[0];
			File targetFile = files[1];
			File tempFile = null;

			if (targetFile.exists()) {
				tempFile = new File(targetFile.getParentFile(), "temp.db");
				targetFile.renameTo(tempFile);
			}

			boolean success = copyFile(sourceFile, targetFile);
			if (success) {
				ShortcutStorage database = new ShortcutStorage(context);
				int count = database.getShortcuts().size();

				if (count > 0) {
					if (tempFile != null && tempFile.exists()) {
						tempFile.delete();
					}
					return count;
				}
			}

			if (tempFile != null && tempFile.exists()) {
				tempFile.renameTo(targetFile);
			}
			return 0;
		}

		protected void onPostExecute(Integer count) {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			AlertDialog dialog = builder.create();

			if (count == 0) {
				dialog.setTitle(R.string.import_failed_title);
				dialog.setMessage(context.getString(R.string.import_failed_message));
			} else {
				dialog.setTitle(R.string.import_success_title);
				if (count == 1) {
					dialog.setMessage(context.getString(R.string.import_success_message_one));
				} else {
					dialog.setMessage(context.getString(R.string.import_success_message, count.intValue()));
				}
			}

			dialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.button_ok), (OnClickListener) null);
			dialog.setCanceledOnTouchOutside(true);

			dialog.show();
		}

	}

	private static boolean copyFile(File sourceFile, File targetFile) {
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
