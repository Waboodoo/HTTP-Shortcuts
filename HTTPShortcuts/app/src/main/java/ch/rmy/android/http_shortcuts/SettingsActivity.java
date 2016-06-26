package ch.rmy.android.http_shortcuts;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.io.File;

import ch.rmy.android.http_shortcuts.dialogs.ChangeLogDialog;
import ch.rmy.android.http_shortcuts.import_export.ExportTask;
import ch.rmy.android.http_shortcuts.import_export.ImportTask;
import ch.rmy.android.http_shortcuts.utils.Settings;

public class SettingsActivity extends BaseActivity {

    private static final String CONTACT_SUBJECT = "HTTP Shortcuts";
    private static final String CONTACT_TEXT = "Dear Roland,\n\n";
    private static final String DEVELOPER_EMAIL = "android@rmy.ch";
    private static final String PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=ch.rmy.android.http_shortcuts";
    private static final String GITHUB_URL = "https://github.com/Waboodoo/HTTP-Shortcuts";

    private static final int REQUEST_PICK_DIR_FOR_EXPORT = 1;
    private static final int REQUEST_PICK_FILE_FOR_IMPORT = 2;

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
                    showExportInstructions();
                    return true;
                }

            });

            final Preference importPreference = findPreference("import");
            importPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                public boolean onPreferenceClick(Preference preference) {
                    showImportInstructions();
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
                    new ChangeLogDialog(getActivity(), false).show();
                    return true;
                }

            });

            final Preference mailPreference = findPreference("mail");
            mailPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                public boolean onPreferenceClick(Preference preference) {
                    sendMail();
                    return true;
                }

            });

            final Preference playStorePreference = findPreference("play_store");
            playStorePreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                public boolean onPreferenceClick(Preference preference) {
                    openPlayStore();
                    return true;
                }

            });

            final Preference githubPreference = findPreference("github");
            githubPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                public boolean onPreferenceClick(Preference preference) {
                    gotoGithub();
                    return true;
                }

            });

            final Preference licensesPreference = findPreference("licenses");
            licensesPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                public boolean onPreferenceClick(Preference preference) {
                    showLicenses();
                    return true;
                }

            });
        }

        private void showExportInstructions() {
            new MaterialDialog.Builder(getActivity())
                    .positiveText(R.string.button_ok)
                    .negativeText(R.string.button_cancel)
                    .content(R.string.export_instructions)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            openFilePickerForExport();
                        }
                    })
                    .show();
        }

        private void showImportInstructions() {
            new MaterialDialog.Builder(getActivity())
                    .positiveText(R.string.button_ok)
                    .negativeText(R.string.button_cancel)
                    .content(R.string.import_instructions)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            openFilePickerForImport();
                        }
                    })
                    .show();
        }

        private void openFilePickerForExport() {
            Intent intent = new Intent(getActivity(), FilePickerActivity.class);
            intent.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
            intent.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
            intent.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
            intent.putExtra(FilePickerActivity.EXTRA_START_PATH, new Settings(getActivity()).getImportExportDirectory());
            startActivityForResult(intent, REQUEST_PICK_DIR_FOR_EXPORT);
        }

        private void openFilePickerForImport() {
            Intent intent = new Intent(getActivity(), FilePickerActivity.class);
            intent.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
            intent.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
            intent.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
            intent.putExtra(FilePickerActivity.EXTRA_START_PATH, new Settings(getActivity()).getImportExportDirectory());
            startActivityForResult(intent, REQUEST_PICK_FILE_FOR_IMPORT);
        }

        private void sendMail() {
            Intent intent = new Intent(Intent.ACTION_SEND);
            String[] recipients = {DEVELOPER_EMAIL};
            intent.putExtra(Intent.EXTRA_EMAIL, recipients);
            intent.putExtra(Intent.EXTRA_SUBJECT, CONTACT_SUBJECT);
            intent.putExtra(Intent.EXTRA_TEXT, CONTACT_TEXT);
            intent.setType("text/html");
            startActivity(Intent.createChooser(intent, getString(R.string.settings_mail)));
        }

        private void openPlayStore() {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_URL));
            startActivity(browserIntent);
        }

        private void gotoGithub() {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL));
            startActivity(browserIntent);
        }

        private void showLicenses() {
            Intent licensesIntent = new Intent(getActivity(), LicensesActivity.class);
            startActivity(licensesIntent);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (resultCode != RESULT_OK) {
                return;
            }
            switch (requestCode) {
                case REQUEST_PICK_DIR_FOR_EXPORT: {
                    Uri uri = data.getData();
                    String directoryPath = uri.getPath();
                    persistPath(directoryPath);
                    startExport(directoryPath);
                    break;
                }
                case REQUEST_PICK_FILE_FOR_IMPORT: {
                    Uri uri = data.getData();
                    String filePath = uri.getPath();
                    String directoryPath = new File(filePath).getParent();
                    persistPath(directoryPath);
                    startImport(filePath);
                    break;
                }
            }
        }

        private void persistPath(String path) {
            new Settings(getActivity()).setImportExportDirectory(path);
        }

        private void startExport(String directoryPath) {
            ExportTask task = new ExportTask(getActivity(), getView());
            task.execute(directoryPath);
        }

        private void startImport(String filePath) {
            ImportTask task = new ImportTask(getActivity(), getView());
            task.execute(filePath);
        }

    }

}
