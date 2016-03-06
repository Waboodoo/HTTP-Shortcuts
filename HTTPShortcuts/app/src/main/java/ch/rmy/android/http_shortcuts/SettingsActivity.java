package ch.rmy.android.http_shortcuts;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ch.rmy.android.http_shortcuts.shortcuts.ShortcutStorage;

public class SettingsActivity extends BaseActivity {

    private static final String CONTACT_SUBJECT = "HTTP Shortcuts";
    private static final String CONTACT_TEXT = "Dear Roland,\n\n";
    private static final String DEVELOPER_EMAIL = "android@rmy.ch";
    private static final String PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=ch.rmy.android.http_shortcuts";
    private static final String GITHUB_URL = "https://github.com/Waboodoo/HTTP-Shortcuts";

    private static final String SHORTCUT_DATABASE_FILE_NAME = "shortcuts";

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SettingsFragment settingsFragment = new SettingsFragment();
        getFragmentManager().beginTransaction().replace(R.id.settings_view, settingsFragment).commit();
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
                    File targetFile = getFileForExport();
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

            final Preference versionPreference = findPreference("version");
            try {
                versionPreference.setSummary(getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName);
            } catch (NameNotFoundException e) {
                versionPreference.setSummary("???");
            }
            versionPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ChangeLogDialog changeLog = new ChangeLogDialog(getActivity(), false);
                    changeLog.show();
                    return true;
                }

            });

            final Preference mailPreference = findPreference("mail");
            mailPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    String[] recipients = {DEVELOPER_EMAIL};
                    intent.putExtra(Intent.EXTRA_EMAIL, recipients);
                    intent.putExtra(Intent.EXTRA_SUBJECT, CONTACT_SUBJECT);
                    intent.putExtra(Intent.EXTRA_TEXT, CONTACT_TEXT);
                    intent.setType("text/html");
                    startActivity(Intent.createChooser(intent, getString(R.string.settings_mail)));
                    return true;
                }

            });

            final Preference playStorePreference = findPreference("play_store");
            playStorePreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                public boolean onPreferenceClick(Preference preference) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_URL));
                    startActivity(browserIntent);
                    return true;
                }

            });

            final Preference githubPreference = findPreference("github");
            githubPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                public boolean onPreferenceClick(Preference preference) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL));
                    startActivity(browserIntent);
                    return true;
                }

            });

        }

        private File getFileForExport() {
            File targetFile;
            int counter = 0;
            do {
                counter++;
                String fileName = SHORTCUT_DATABASE_FILE_NAME + (counter == 1 ? "" : "_" + counter) + ".db";
                targetFile = new File(Environment.getExternalStorageDirectory(), fileName);
            } while (targetFile.exists());
            return targetFile;
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
