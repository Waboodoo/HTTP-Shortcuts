package ch.rmy.android.http_shortcuts;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.satsuware.usefulviews.LabelledSpinner;

import net.dinglisch.ipack.IpackKeys;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import ch.rmy.android.http_shortcuts.dialogs.IconNameChangeDialog;
import ch.rmy.android.http_shortcuts.icons.IconSelector;
import ch.rmy.android.http_shortcuts.icons.IconView;
import ch.rmy.android.http_shortcuts.icons.Icons;
import ch.rmy.android.http_shortcuts.key_value_pairs.KeyValueList;
import ch.rmy.android.http_shortcuts.key_value_pairs.KeyValuePairFactory;
import ch.rmy.android.http_shortcuts.listeners.OnIconSelectedListener;
import ch.rmy.android.http_shortcuts.realm.Controller;
import ch.rmy.android.http_shortcuts.realm.models.Header;
import ch.rmy.android.http_shortcuts.realm.models.Parameter;
import ch.rmy.android.http_shortcuts.realm.models.Shortcut;
import ch.rmy.android.http_shortcuts.realm.models.Variable;
import ch.rmy.android.http_shortcuts.utils.ArrayUtil;
import ch.rmy.android.http_shortcuts.utils.GsonUtil;
import ch.rmy.android.http_shortcuts.utils.IntentUtil;
import ch.rmy.android.http_shortcuts.utils.OnItemChosenListener;
import ch.rmy.android.http_shortcuts.utils.ShortcutUIUtils;
import ch.rmy.android.http_shortcuts.utils.Validation;
import ch.rmy.android.http_shortcuts.utils.ViewUtil;
import ch.rmy.android.http_shortcuts.variables.VariableFormatter;
import ch.rmy.curlcommand.CurlCommand;

import static ch.rmy.android.http_shortcuts.realm.models.Shortcut.TEMPORARY_ID;

/**
 * The activity to create/edit shortcuts.
 *
 * @author Roland Meyer
 */
@SuppressLint("InflateParams")
public class EditorActivity extends BaseActivity {

    public static final String EXTRA_SHORTCUT_ID = "ch.rmy.android.http_shortcuts.EditorActivity.shortcut_id";
    public static final String EXTRA_CURL_COMMAND = "ch.rmy.android.http_shortcuts.EditorActivity.curl_command";

    private final static int SELECT_ICON = 1;
    private final static int SELECT_IPACK_ICON = 3;
    private static final String STATE_JSON_SHORTCUT = "shortcut_json";
    private static final String STATE_INITIAL_ICON = "initial_icon";

    private long shortcutId;

    private Controller controller;
    private Shortcut oldShortcut;
    private Shortcut shortcut;
    private List<Variable> variables;

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
    IconView iconView;
    @Bind(R.id.post_params_container)
    LinearLayout postParamsContainer;
    @Bind(R.id.post_parameter_list)
    KeyValueList<Parameter> parameterList;
    @Bind(R.id.custom_headers_list)
    KeyValueList<Header> customHeaderList;
    @Bind(R.id.input_custom_body)
    EditText customBodyView;
    @Bind(R.id.input_accept_all_certificates)
    CheckBox acceptCertificatesCheckbox;

    private final OnItemChosenListener itemChosenListener = new OnItemChosenListener() {
        @Override
        public void onSelectionChanged() {
            compileShortcut();
            updateUI();
        }
    };

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        destroyer.own(parameterList);
        destroyer.own(customHeaderList);

        controller = destroyer.own(new Controller(this));
        variables = controller.getVariables();

        shortcutId = getIntent().getLongExtra(EXTRA_SHORTCUT_ID, 0);
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_JSON_SHORTCUT)) {
            shortcut = GsonUtil.fromJson(savedInstanceState.getString(STATE_JSON_SHORTCUT), Shortcut.class);
        } else {
            shortcut = shortcutId == 0 ? Shortcut.createNew() : controller.getDetachedShortcutById(shortcutId);
        }
        if (shortcut == null) {
            finish();
            return;
        }
        oldShortcut = shortcutId == 0 ? Shortcut.createNew() : controller.getDetachedShortcutById(shortcutId);
        if (shortcut.isNew()) {

            Serializable curlCommand = getIntent().getSerializableExtra(EXTRA_CURL_COMMAND);
            if (curlCommand != null) {
                extractFromCurlCommand(shortcut, (CurlCommand) curlCommand);
            }

            if (shortcut.getIconName() == null) {
                shortcut.setIconName(Icons.getRandomIcon(getContext()));
                oldShortcut.setIconName(shortcut.getIconName());
            } else if (savedInstanceState != null && savedInstanceState.containsKey(STATE_INITIAL_ICON)) {
                oldShortcut.setIconName(savedInstanceState.getString(STATE_INITIAL_ICON));
            }
        }

        initViews();
    }

    private void extractFromCurlCommand(Shortcut shortcut, CurlCommand curlCommand) {
        shortcut.setUrl(curlCommand.getUrl());
        shortcut.setMethod(curlCommand.getMethod());
        shortcut.setBodyContent(curlCommand.getData());
        shortcut.setUsername(curlCommand.getUsername());
        shortcut.setPassword(curlCommand.getPassword());
        if (curlCommand.getTimeout() != 0) {
            shortcut.setTimeout(curlCommand.getTimeout());
        }
        for (Map.Entry<String, String> header : curlCommand.getHeaders().entrySet()) {
            shortcut.getHeaders().add(Header.createNew(header.getKey(), header.getValue()));
        }
    }

    private void initViews() {
        nameView.setText(shortcut.getName());
        descriptionView.setText(shortcut.getDescription());
        urlView.setText(shortcut.getUrl());
        usernameView.setText(shortcut.getUsername());
        passwordView.setText(shortcut.getPassword());
        customBodyView.setText(shortcut.getBodyContent());

        bindVariableFormatter(urlView);
        bindVariableFormatter(usernameView);
        bindVariableFormatter(passwordView);
        bindVariableFormatter(customBodyView);

        methodView.setItemsArray(Shortcut.METHOD_OPTIONS);
        ViewUtil.hideErrorLabel(methodView);
        methodView.setOnItemChosenListener(itemChosenListener);
        methodView.setSelection(ArrayUtil.findIndex(Shortcut.METHOD_OPTIONS, shortcut.getMethod()));

        parameterList.addItems(shortcut.getParameters());
        parameterList.setButtonText(R.string.button_add_post_param);
        parameterList.setAddDialogTitle(R.string.title_post_param_add);
        parameterList.setEditDialogTitle(R.string.title_post_param_edit);
        parameterList.setKeyLabel(R.string.label_post_param_key);
        parameterList.setValueLabel(R.string.label_post_param_value);
        parameterList.setItemFactory(new KeyValuePairFactory<Parameter>() {
            @Override
            public Parameter create(String key, String value) {
                return Parameter.createNew(key, value);
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
                return Header.createNew(key, value);
            }
        });
        customHeaderList.setSuggestions(Header.SUGGESTED_KEYS);

        feedbackView.setItemsArray(ShortcutUIUtils.getFeedbackOptions(this));
        feedbackView.setOnItemChosenListener(itemChosenListener);
        ViewUtil.hideErrorLabel(feedbackView);
        feedbackView.setSelection(ArrayUtil.findIndex(Shortcut.FEEDBACK_OPTIONS, shortcut.getFeedback()));

        timeoutView.setItemsArray(ShortcutUIUtils.getTimeoutOptions(this));
        ViewUtil.hideErrorLabel(timeoutView);
        timeoutView.setSelection(ArrayUtil.findIndex(Shortcut.TIMEOUT_OPTIONS, shortcut.getTimeout()));

        retryPolicyView.setItemsArray(ShortcutUIUtils.getRetryPolicyOptions(this));
        ViewUtil.hideErrorLabel(retryPolicyView);
        retryPolicyView.setSelection(ArrayUtil.findIndex(Shortcut.RETRY_POLICY_OPTIONS, shortcut.getRetryPolicy()));

        acceptCertificatesCheckbox.setChecked(shortcut.isAcceptAllCertificates());

        iconView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                openIconSelectionDialog();
            }
        });

        setTitle(shortcut.isNew() ? R.string.create_shortcut : R.string.edit_shortcut);
        updateUI();
    }

    private void bindVariableFormatter(EditText editText) {
        destroyer.own(VariableFormatter.bind(editText, variables));
    }

    private void updateUI() {
        iconView.setImageURI(shortcut.getIconURI(this), shortcut.getIconName());
        retryPolicyView.setVisibility(shortcut.isRetryAllowed() ? View.VISIBLE : View.GONE);
        postParamsContainer.setVisibility(Shortcut.METHOD_GET.equals(shortcut.getMethod()) ? View.GONE : View.VISIBLE);
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
                trySave();
                return true;
            }
            case R.id.action_test_shortcut: {
                test();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void trySave() {
        compileShortcut();
        if (validate(false)) {
            shortcut.setId(shortcutId);
            Shortcut persistedShortcut = controller.persist(shortcut);
            Intent returnIntent = new Intent();
            returnIntent.putExtra(EXTRA_SHORTCUT_ID, persistedShortcut.getId());
            setResult(RESULT_OK, returnIntent);
            IconNameChangeDialog dialog = new IconNameChangeDialog(this);
            if (!oldShortcut.isNew() && nameOrIconChanged() && dialog.shouldShow()) {
                dialog.show(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        finish();
                    }
                });
            } else {
                finish();
            }
        }
    }

    private boolean validate(boolean testOnly) {
        if (!testOnly && Validation.isEmpty(shortcut.getName())) {
            nameView.setError(getString(R.string.validation_name_not_empty));
            ViewUtil.focus(nameView);
            return false;
        }
        if (!Validation.isValidUrl(shortcut.getUrl())) {
            urlView.setError(getString(R.string.validation_url_invalid));
            ViewUtil.focus(urlView);
            return false;
        }
        return true;
    }

    private boolean nameOrIconChanged() {
        return !TextUtils.equals(oldShortcut.getName(), shortcut.getName()) || !TextUtils.equals(oldShortcut.getIconName(), shortcut.getIconName());
    }

    private void test() {
        compileShortcut();
        if (validate(true)) {
            shortcut.setId(TEMPORARY_ID);
            controller.persist(shortcut);
            Intent intent = IntentUtil.createIntent(this, TEMPORARY_ID);
            startActivity(intent);
        }
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
                                break;
                            case 1:
                                openImagePicker();
                                break;
                            case 2:
                                openIpackPicker();
                                break;
                        }
                    }
                })
                .show();
    }

    private void openBuiltInIconSelectionDialog() {
        IconSelector iconSelector = new IconSelector(this, new OnIconSelectedListener() {

            @Override
            public void onIconSelected(String iconName) {
                shortcut.setIconName(iconName);
                updateUI();
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
        Intent iconIntent = Intent.createChooser(new Intent(IpackKeys.Actions.ICON_SELECT), getString(R.string.choose_ipack));
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
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
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
        shortcut.setBodyContent(customBodyView.getText().toString());
        shortcut.setFeedback(Shortcut.FEEDBACK_OPTIONS[feedbackView.getSpinner().getSelectedItemPosition()]);
        shortcut.setTimeout(Shortcut.TIMEOUT_OPTIONS[timeoutView.getSpinner().getSelectedItemPosition()]);
        shortcut.setRetryPolicy(Shortcut.RETRY_POLICY_OPTIONS[retryPolicyView.getSpinner().getSelectedItemPosition()]);
        shortcut.setAcceptAllCertificates(acceptCertificatesCheckbox.isChecked());

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

        if (resultCode != RESULT_OK) {
            return;
        }

        if (requestCode == SELECT_ICON) {
            //FIXME: Generate better file names
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
                out.flush();

                shortcut.setIconName(iconName);
            } catch (Exception e) {
                e.printStackTrace();
                shortcut.setIconName(null);
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
            shortcut.setIconName(uri.toString());
        }
        updateUI();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        compileShortcut();
        outState.putString(STATE_JSON_SHORTCUT, GsonUtil.toJson(shortcut));
        outState.putString(STATE_INITIAL_ICON, oldShortcut.getIconName());
    }
}
