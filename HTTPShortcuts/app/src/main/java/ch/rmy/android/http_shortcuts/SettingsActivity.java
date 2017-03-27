package ch.rmy.android.http_shortcuts;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
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
import ch.rmy.android.http_shortcuts.realm.Controller;
import ch.rmy.android.http_shortcuts.realm.models.Base;
import ch.rmy.android.http_shortcuts.utils.GsonUtil;
import ch.rmy.android.http_shortcuts.utils.MenuDialogBuilder;
import ch.rmy.android.http_shortcuts.utils.Settings;

public class SettingsActivity extends BaseActivity {

    public static final int REQUEST_SETTINGS = 52;
    public static final String EXTRA_THEME_CHANGED = "theme_changed";

    private static final String CONTACT_SUBJECT = "HTTP Shortcuts";
    private static final String CONTACT_TEXT = "Hey Roland,\n\n";
    private static final String DEVELOPER_EMAIL = "android@rmy.ch";
    private static final String PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=ch.rmy.android.http_shortcuts";
    private static final String GITHUB_URL = "https://github.com/Waboodoo/HTTP-Shortcuts";

    private static final int REQUEST_PICK_DIR_FOR_EXPORT = 1;
    private static final int REQUEST_PICK_FILE_FOR_IMPORT = 2;
    private static final int REQUEST_IMPORT_FROM_DOCUMENTS = 3;

    private static final String IMPORT_EXPORT_FILE_TYPE = "text/plain";

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
                    updateSummary(clickBehaviorPreference, newValue);
                    return true;
                }

            });
            updateSummary(clickBehaviorPreference, null);

            final ListPreference themePreference = (ListPreference) findPreference("theme");
            themePreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    updateSummary(themePreference, newValue);
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(EXTRA_THEME_CHANGED, true);
                    getActivity().setResult(RESULT_OK, returnIntent);
                    getActivity().finish();
                    getActivity().overridePendingTransition(0, 0);
                    return true;
                }

            });
            updateSummary(themePreference, null);

            final Preference exportPreference = findPreference("export");
            exportPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                public boolean onPreferenceClick(Preference preference) {
                    showExportOptions();
                    return true;
                }

            });

            final Preference importPreference = findPreference("import");
            importPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                public boolean onPreferenceClick(Preference preference) {
                    showImportOptions();
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

        private void updateSummary(ListPreference preference, Object value) {
            if (value == null) {
                value = preference.getValue();
            }
            int index = preference.findIndexOfValue((String) value);
            if (index == -1) {
                index = 0;
            }
            preference.setSummary(preference.getEntries()[index]);
        }

        private void showExportOptions() {
            new MenuDialogBuilder(getActivity())
                    .title(R.string.title_export)
                    .item(R.string.button_export_to_filesystem, new MenuDialogBuilder.Action() {
                        @Override
                        public void execute() {
                            showExportInstructions();
                        }
                    })
                    .item(R.string.button_export_send_to, new MenuDialogBuilder.Action() {
                        @Override
                        public void execute() {
                            sendExport();
                        }
                    }).show();
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

        private void sendExport() {
            Controller controller = null;
            try {
                controller = new Controller();
                Base base = controller.exportBase();
                String data = GsonUtil.exportData(base);
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType(IMPORT_EXPORT_FILE_TYPE);
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, data);
                startActivity(Intent.createChooser(sharingIntent, getString(R.string.title_export)));
            } finally {
                if (controller != null) {
                    controller.destroy();
                }
            }
        }

        private void showImportOptions() {
            new MenuDialogBuilder(getActivity())
                    .title(R.string.title_import)
                    .item(R.string.button_import_from_filesystem, new MenuDialogBuilder.Action() {
                        @Override
                        public void execute() {
                            showImportInstructions();
                        }
                    })
                    .item(R.string.button_import_from_general, new MenuDialogBuilder.Action() {
                        @Override
                        public void execute() {
                            openGeneralPickerForImport();
                        }
                    }).show();
        }

        private void showImportInstructions() {
            new MaterialDialog.Builder(getActivity())
                    .positiveText(R.string.button_ok)
                    .negativeText(R.string.button_cancel)
                    .content(R.string.import_instructions)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            openLocalFilePickerForImport();
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

        private void openLocalFilePickerForImport() {
            Intent intent = new Intent(getActivity(), FilePickerActivity.class);
            intent.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
            intent.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
            intent.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
            intent.putExtra(FilePickerActivity.EXTRA_START_PATH, new Settings(getActivity()).getImportExportDirectory());
            startActivityForResult(intent, REQUEST_PICK_FILE_FOR_IMPORT);
        }

        private void openGeneralPickerForImport() {
            Intent pickerIntent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                pickerIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            } else {
                pickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
            }
            pickerIntent.setType(IMPORT_EXPORT_FILE_TYPE);
            pickerIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickerIntent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(pickerIntent, REQUEST_IMPORT_FROM_DOCUMENTS);
        }

        private void sendMail() {
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + DEVELOPER_EMAIL));
            String[] recipients = {DEVELOPER_EMAIL};
            intent.putExtra(Intent.EXTRA_EMAIL, recipients);
            intent.putExtra(Intent.EXTRA_SUBJECT, CONTACT_SUBJECT);
            intent.putExtra(Intent.EXTRA_TEXT, CONTACT_TEXT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
        public void onActivityResult(int requestCode, int resultCode, Intent intent) {
            if (resultCode != RESULT_OK || intent == null) {
                return;
            }
            switch (requestCode) {
                case REQUEST_PICK_DIR_FOR_EXPORT: {
                    Uri uri = intent.getData();
                    String directoryPath = uri.getPath();
                    persistPath(directoryPath);
                    startExport(directoryPath);
                    break;
                }
                case REQUEST_PICK_FILE_FOR_IMPORT: {
                    Uri uri = intent.getData();
                    String filePath = uri.getPath();
                    String directoryPath = new File(filePath).getParent();
                    persistPath(directoryPath);
                    startImport(uri);
                    break;
                }
                case REQUEST_IMPORT_FROM_DOCUMENTS: {
                    Uri uri = intent.getData();
                    startImport(uri);
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

        private void startImport(Uri uri) {
            ImportTask task = new ImportTask(getActivity(), getView());
            task.execute(uri);
        }

    }

}
