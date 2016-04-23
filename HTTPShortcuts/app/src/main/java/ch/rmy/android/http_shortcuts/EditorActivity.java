package ch.rmy.android.http_shortcuts;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.farbod.labelledspinner.LabelledSpinner;

import net.dinglisch.ipack.IpackKeys;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import butterknife.Bind;
import ch.rmy.android.http_shortcuts.http.HttpRequester;
import ch.rmy.android.http_shortcuts.icons.IconSelector;
import ch.rmy.android.http_shortcuts.key_value_pairs.KeyValueList;
import ch.rmy.android.http_shortcuts.key_value_pairs.KeyValuePairFactory;
import ch.rmy.android.http_shortcuts.listeners.OnIconSelectedListener;
import ch.rmy.android.http_shortcuts.realm.Controller;
import ch.rmy.android.http_shortcuts.realm.models.Header;
import ch.rmy.android.http_shortcuts.realm.models.Parameter;
import ch.rmy.android.http_shortcuts.realm.models.Shortcut;
import ch.rmy.android.http_shortcuts.utils.Validation;
import ch.rmy.android.http_shortcuts.utils.ViewUtil;

/**
 * The activity to create/edit shortcuts.
 *
 * @author Roland Meyer
 */
@SuppressLint("InflateParams")
public class EditorActivity extends BaseActivity {

    public final static String EXTRA_SHORTCUT_ID = "shortcut_id";
    private final static int SELECT_ICON = 1;
    private final static int SELECT_IPACK_ICON = 3;

    private Controller controller;
    private Shortcut oldShortcut;
    private Shortcut shortcut;

    @Bind(R.id.input_method)
    LabelledSpinner methodView;
    @Bind(R.id.input_feedback)
    LabelledSpinner feedbackView;
    @Bind(R.id.input_timeout)
    LabelledSpinner timeoutView;
    @Bind(R.id.input_retry_policy)
    LabelledSpinner retryPolicyView;
    @Bind(R.id.input_shortcut_name)
    EditText nameView;
    @Bind(R.id.input_description)
    EditText descriptionView;
    @Bind(R.id.input_url)
    EditText urlView;
    @Bind(R.id.input_username)
    EditText usernameView;
    @Bind(R.id.input_password)
    EditText passwordView;
    @Bind(R.id.input_icon)
    ImageView iconView;
    @Bind(R.id.post_params_container)
    LinearLayout postParamsContainer;
    @Bind(R.id.post_parameter_list)
    KeyValueList<Parameter> parameterList;
    @Bind(R.id.custom_headers_list)
    KeyValueList<Header> customHeaderList;
    @Bind(R.id.input_custom_body)
    EditText customBodyView;

    private String selectedIcon;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        controller = new Controller(this);

        long shortcutId = getIntent().getLongExtra(EXTRA_SHORTCUT_ID, 0);
        if (shortcutId == 0) {
            shortcut = Shortcut.createNew();
            oldShortcut = Shortcut.createNew();
        } else {
            shortcut = controller.getDetachedShortcutById(shortcutId);
            oldShortcut = controller.getDetachedShortcutById(shortcutId);
            // TODO: Add null check
        }

        nameView.setText(shortcut.getName());
        descriptionView.setText(shortcut.getDescription());
        urlView.setText(shortcut.getUrl());
        usernameView.setText(shortcut.getUsername());
        passwordView.setText(shortcut.getPassword());
        customBodyView.setText(shortcut.getBodyContent());

        methodView.setItemsArray(Shortcut.METHOD_OPTIONS);
        hideErrorLabel(methodView);
        methodView.setOnItemChosenListener(new LabelledSpinner.OnItemChosenListener() {
            @Override
            public void onItemChosen(View labelledSpinner, AdapterView<?> adapterView, View itemView, int position, long id) {
                boolean methodIsGet = Shortcut.METHOD_OPTIONS[methodView.getSpinner().getSelectedItemPosition()].equals(Shortcut.METHOD_GET);
                postParamsContainer.setVisibility(methodIsGet ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onNothingChosen(View labelledSpinner, AdapterView<?> adapterView) {

            }
        });
        for (int i = 0; i < Shortcut.METHOD_OPTIONS.length; i++) {
            if (Shortcut.METHOD_OPTIONS[i].equals(shortcut.getMethod())) {
                methodView.setSelection(i);
                break;
            }
        }

        if (Shortcut.METHOD_GET.equals(shortcut.getMethod())) {
            postParamsContainer.setVisibility(View.GONE);
        } else {
            postParamsContainer.setVisibility(View.VISIBLE);
        }
        parameterList.addItems(shortcut.getParameters());
        parameterList.setButtonText(R.string.button_add_post_param);
        parameterList.setAddDialogTitle(R.string.title_post_param_add);
        parameterList.setEditDialogTitle(R.string.title_post_param_edit);
        parameterList.setKeyLabel(R.string.label_post_param_key);
        parameterList.setValueLabel(R.string.label_post_param_value);
        parameterList.setItemFactory(new KeyValuePairFactory<Parameter>() {
            @Override
            public Parameter create(String key, String value) {
                Parameter parameter = new Parameter();
                parameter.setKey(key);
                parameter.setValue(value);
                return parameter;
            }
        });

        customHeaderList.addItems(shortcut.getHeaders());
        customHeaderList.setButtonText(R.string.button_add_custom_header);
        customHeaderList.setAddDialogTitle(R.string.title_custom_header_add);
        customHeaderList.setEditDialogTitle(R.string.title_custom_header_edit);
        customHeaderList.setKeyLabel(R.string.label_custom_header_key);
        customHeaderList.setValueLabel(R.string.label_custom_header_value);
        customHeaderList.setItemFactory(new KeyValuePairFactory<Header>() {
            @Override
            public Header create(String key, String value) {
                Header header = new Header();
                header.setKey(key);
                header.setValue(value);
                return header;
            }
        });

        String[] feedbackStrings = new String[Shortcut.FEEDBACK_OPTIONS.length];
        for (int i = 0; i < Shortcut.FEEDBACK_OPTIONS.length; i++) {
            feedbackStrings[i] = getText(Shortcut.FEEDBACK_RESOURCES[i]).toString();
        }
        feedbackView.setItemsArray(feedbackStrings);
        hideErrorLabel(feedbackView);
        for (int i = 0; i < Shortcut.FEEDBACK_OPTIONS.length; i++) {
            if (Shortcut.FEEDBACK_OPTIONS[i].equals(shortcut.getFeedback())) {
                feedbackView.setSelection(i);
                break;
            }
        }

        String[] timeoutStrings = new String[Shortcut.TIMEOUT_OPTIONS.length];
        for (int i = 0; i < Shortcut.TIMEOUT_OPTIONS.length; i++) {
            timeoutStrings[i] = String.format(getText(Shortcut.TIMEOUT_RESOURCES[i]).toString(), Shortcut.TIMEOUT_OPTIONS[i] / 1000);
        }
        timeoutView.setItemsArray(timeoutStrings);
        hideErrorLabel(timeoutView);
        for (int i = 0; i < Shortcut.TIMEOUT_OPTIONS.length; i++) {
            if (Shortcut.TIMEOUT_OPTIONS[i] == shortcut.getTimeout()) {
                timeoutView.setSelection(i);
                break;
            }
        }

        String[] retryPolicyStrings = new String[Shortcut.RETRY_POLICY_OPTIONS.length];
        for (int i = 0; i < Shortcut.RETRY_POLICY_OPTIONS.length; i++) {
            retryPolicyStrings[i] = getText(Shortcut.RETRY_POLICY_RESOURCES[i]).toString();
        }
        retryPolicyView.setItemsArray(retryPolicyStrings);
        hideErrorLabel(retryPolicyView);
        for (int i = 0; i < Shortcut.RETRY_POLICY_OPTIONS.length; i++) {
            if (Shortcut.RETRY_POLICY_OPTIONS[i].equals(shortcut.getRetryPolicy())) {
                retryPolicyView.setSelection(i);
                break;
            }
        }

        iconView.setImageURI(shortcut.getIconURI(this));
        if (shortcut.getIconName() != null && shortcut.getIconName().startsWith("white_")) {
            iconView.setBackgroundColor(Color.BLACK);
        } else {
            iconView.setBackgroundColor(Color.TRANSPARENT);
        }
        iconView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                openIconSelectionDialog();
            }
        });
        selectedIcon = shortcut.getIconName();

        if (shortcut.isNew()) {
            setTitle(R.string.create_shortcut);
        } else {
            setTitle(R.string.edit_shortcut);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.editor_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected int getNavigateUpIcon() {
        return R.drawable.ic_clear;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                confirmClose();
                return true;
            }
            case R.id.action_save_shortcut: {
                compileShortcut();
                if (validate(false)) {
                    Shortcut persistedShortcut = controller.persist(shortcut);
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(EXTRA_SHORTCUT_ID, persistedShortcut.getId());
                    setResult(RESULT_OK, returnIntent);
                    finish();
                }
                return true;
            }
            case R.id.action_test_shortcut: {
                compileShortcut();
                if (validate(true)) {
                    HttpRequester.executeShortcut(this, shortcut);
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean validate(boolean testOnly) {
        if (!testOnly && Validation.isEmpty(shortcut.getName())) {
            nameView.setError(getText(R.string.validation_name_not_empty));
            ViewUtil.focus(nameView);
            return false;
        }
        if (!Validation.isValidUrl(shortcut.getUrl())) {
            urlView.setError(getText(R.string.validation_url_invalid));
            ViewUtil.focus(urlView);
            return false;
        }
        return true;
    }

    private void openIconSelectionDialog() {
        (new MaterialDialog.Builder(this))
                .title(R.string.change_icon)
                .items(R.array.context_menu_choose_icon)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        switch (which) {
                            case 0:
                                openBuiltInIconSelectionDialog();
                                return;
                            case 1:
                                openImagePicker();
                                return;
                            case 2:
                                openIpackPicker();
                                return;
                        }
                    }
                })
                .show();
    }

    private void openBuiltInIconSelectionDialog() {
        IconSelector iconSelector = new IconSelector(this, new OnIconSelectedListener() {

            @Override
            public void onIconSelected(String resourceName) {
                iconView.setImageResource(getResources().getIdentifier(resourceName, "drawable", getPackageName()));

                if (resourceName.startsWith("white_")) {
                    iconView.setBackgroundColor(Color.BLACK);
                } else {
                    iconView.setBackgroundColor(Color.TRANSPARENT);
                }

                selectedIcon = resourceName;
            }

        });
        iconSelector.show();
    }

    private void openImagePicker() {
        // Workaround for Kitkat (thanks to http://stackoverflow.com/a/20186938/1082111)
        Intent imageIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imageIntent.setType("image/*");
        startActivityForResult(imageIntent, SELECT_ICON);
    }

    private void openIpackPicker() {
        Intent iconIntent = Intent.createChooser(new Intent(IpackKeys.Actions.ICON_SELECT), getText(R.string.choose_ipack));
        startActivityForResult(iconIntent, SELECT_IPACK_ICON);
    }

    @Override
    public void onBackPressed() {
        confirmClose();
    }

    private void confirmClose() {
        compileShortcut();
        if (hasChanges()) {
            (new MaterialDialog.Builder(this))
                    .content(R.string.confirm_discard_changes_message)
                    .positiveText(R.string.dialog_discard)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(MaterialDialog dialog, DialogAction which) {
                            cancelAndClose();
                        }
                    })
                    .negativeText(R.string.dialog_cancel)
                    .show();
        } else {
            cancelAndClose();
        }
    }

    private boolean hasChanges() {
        return !oldShortcut.equals(shortcut);
    }

    private void compileShortcut() {
        shortcut.setName(nameView.getText().toString().trim());
        shortcut.setUrl(urlView.getText().toString());
        shortcut.setMethod(Shortcut.METHOD_OPTIONS[methodView.getSpinner().getSelectedItemPosition()]);
        shortcut.setDescription(descriptionView.getText().toString().trim());
        shortcut.setPassword(passwordView.getText().toString());
        shortcut.setUsername(usernameView.getText().toString());
        shortcut.setIconName(selectedIcon);
        shortcut.setBodyContent(customBodyView.getText().toString());
        shortcut.setFeedback(Shortcut.FEEDBACK_OPTIONS[feedbackView.getSpinner().getSelectedItemPosition()]);
        shortcut.setTimeout(Shortcut.TIMEOUT_OPTIONS[timeoutView.getSpinner().getSelectedItemPosition()]);
        shortcut.setRetryPolicy(Shortcut.RETRY_POLICY_OPTIONS[retryPolicyView.getSpinner().getSelectedItemPosition()]);

        shortcut.getParameters().clear();
        shortcut.getParameters().addAll(parameterList.getItems());
        shortcut.getHeaders().clear();
        shortcut.getHeaders().addAll(customHeaderList.getItems());
    }

    private void cancelAndClose() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_ICON) {

                String iconName = Integer.toHexString((int) Math.floor(Math.random() * 1000000)) + ".png";

                InputStream in = null;
                OutputStream out = null;
                try {
                    in = getContentResolver().openInputStream(intent.getData());
                    Bitmap bitmap = BitmapFactory.decodeStream(in);
                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 96, 96, false);
                    if (bitmap != resizedBitmap) {
                        bitmap.recycle();
                    }

                    out = openFileOutput(iconName, 0);
                    resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    iconView.setImageBitmap(resizedBitmap);
                    iconView.setBackgroundColor(0);
                    out.flush();

                    selectedIcon = iconName;
                } catch (Exception e) {
                    e.printStackTrace();
                    iconView.setImageResource(Shortcut.DEFAULT_ICON);
                    iconView.setBackgroundColor(0);
                    selectedIcon = null;
                    showSnackbar(getString(R.string.error_set_image));
                } finally {
                    try {
                        if (in != null) {
                            in.close();
                        }
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {
                    }
                }
            } else if (requestCode == SELECT_IPACK_ICON) {
                String ipackageName = intent.getData().getAuthority();
                int id = intent.getIntExtra(IpackKeys.Extras.ICON_ID, -1);
                Uri uri = Uri.parse("android.resource://" + ipackageName + "/" + id);
                iconView.setImageURI(uri);
                iconView.setBackgroundColor(0);

                selectedIcon = uri.toString();
            }
        }
    }

    private void hideErrorLabel(LabelledSpinner spinner) {
        spinner.getErrorLabel().setVisibility(View.GONE);
    }

}
