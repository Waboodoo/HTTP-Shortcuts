package ch.rmy.android.http_shortcuts;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.dinglisch.ipack.IpackKeys;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;
import ch.rmy.android.http_shortcuts.shortcuts.PostParameter;
import ch.rmy.android.http_shortcuts.shortcuts.PostParameterAdapter;
import ch.rmy.android.http_shortcuts.shortcuts.Shortcut;
import ch.rmy.android.http_shortcuts.shortcuts.ShortcutStorage;

/**
 * The activity to create/edit shortcuts.
 * 
 * @author Roland Meyer
 */
public class EditorActivity extends Activity implements OnClickListener, OnItemSelectedListener, OnItemClickListener, TextWatcher {

	public final static String EXTRA_SHORTCUT_ID = "shortcut_id";
	private final static int SELECT_ICON = 1;
	private final static int SELECT_IPACK_ICON = 3;
	public final static int EDIT_SHORTCUT = 2;

	private ShortcutStorage shortcutStorage;
	private Shortcut shortcut;
	private PostParameterAdapter postParameterAdapter;

	private EditText nameView;
	private EditText descriptionView;
	private EditText urlView;
	private EditText usernameView;
	private EditText passwordView;
	private ImageView iconView;
	private Spinner methodView;
	private Spinner feedbackView;
	private LinearLayout postParamsContainer;
	private ListView postParameterList;
	private Button postParameterAddButton;

	private String selectedMethod;
	private int selectedFeedback;
	private String selectedIcon;

	private boolean hasChanges;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_editor);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		shortcutStorage = new ShortcutStorage(this);
		long shortcutID = getIntent().getLongExtra(EXTRA_SHORTCUT_ID, 0);
		if (shortcutID == 0) {
			shortcut = shortcutStorage.createShortcut();
		} else {
			shortcut = shortcutStorage.getShortcutByID(shortcutID);
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			window.setStatusBarColor(getResources().getColor(R.color.dark_blue));
		}

		nameView = (EditText) findViewById(R.id.input_shortcut_name);
		descriptionView = (EditText) findViewById(R.id.input_description);
		urlView = (EditText) findViewById(R.id.input_url);
		usernameView = (EditText) findViewById(R.id.input_username);
		passwordView = (EditText) findViewById(R.id.input_password);
		iconView = (ImageView) findViewById(R.id.input_icon);
		postParamsContainer = (LinearLayout) findViewById(R.id.post_params_container);
		postParameterList = (ListView) findViewById(R.id.post_parameter_list);
		postParameterAddButton = (Button) findViewById(R.id.button_add_post_param);

		nameView.setText(shortcut.getName());
		descriptionView.setText(shortcut.getDescription());
		urlView.setText(shortcut.getProtocol() + "://" + shortcut.getURL());
		usernameView.setText(shortcut.getUsername());
		passwordView.setText(shortcut.getPassword());

		nameView.addTextChangedListener(this);
		descriptionView.addTextChangedListener(this);
		urlView.addTextChangedListener(this);
		usernameView.addTextChangedListener(this);
		passwordView.addTextChangedListener(this);

		methodView = (Spinner) findViewById(R.id.input_method);
		selectedMethod = shortcut.getMethod();
		SpinnerAdapter methodAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, Shortcut.METHODS);
		methodView.setAdapter(methodAdapter);
		for (int i = 0; i < Shortcut.METHODS.length; i++) {
			if (Shortcut.METHODS[i].equals(shortcut.getMethod())) {
				methodView.setSelection(i);
				break;
			}
		}
		methodView.setOnItemSelectedListener(this);

		if (selectedMethod == Shortcut.METHOD_GET) {
			postParamsContainer.setVisibility(View.GONE);
		} else {
			postParamsContainer.setVisibility(View.VISIBLE);
		}
		postParameterAdapter = new PostParameterAdapter(this);
		postParameterList.setAdapter(postParameterAdapter);
		postParameterAdapter.addAll(shortcutStorage.getPostParametersByID(shortcutID));
		postParameterAddButton.setOnClickListener(this);
		postParameterList.setOnItemClickListener(this);

		feedbackView = (Spinner) findViewById(R.id.input_feedback);
		String[] feedbackStrings = new String[Shortcut.FEEDBACKS.length];
		for (int i = 0; i < Shortcut.FEEDBACKS.length; i++) {
			feedbackStrings[i] = getText(Shortcut.FEEDBACK_RESOURCES[i]).toString();
		}
		SpinnerAdapter feedbackAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, feedbackStrings);
		feedbackView.setOnItemSelectedListener(this);
		feedbackView.setAdapter(feedbackAdapter);
		for (int i = 0; i < Shortcut.FEEDBACKS.length; i++) {
			if (Shortcut.FEEDBACKS[i] == shortcut.getFeedback()) {
				feedbackView.setSelection(i);
				break;
			}
		}
		selectedFeedback = shortcut.getFeedback();

		iconView.setImageURI(shortcut.getIconURI(this));
		iconView.setOnClickListener(this);
		registerForContextMenu(iconView);
		selectedIcon = shortcut.getIconName();

		if (shortcut.isNew()) {
			getActionBar().setTitle(R.string.create_shortcut);
		} else {
			getActionBar().setTitle(R.string.edit_shortcut);
		}

		hasChanges = false;
	}

	@Override
	public void onResume() {
		super.onResume();
		setListViewHeightBasedOnChildren(postParameterList);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.editor_activity_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			confirmClose();
			return true;
		case R.id.action_save_shortcut:
			saveAndClose();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		if (v.equals(iconView)) {
			openContextMenu(v);
		} else if (v.equals(postParameterAddButton)) {
			LayoutInflater inflater = LayoutInflater.from(this);
			View layout = inflater.inflate(R.layout.dialog_edit_post_parameter, null);

			final EditText keyField = (EditText) layout.findViewById(R.id.input_post_param_key);

			final EditText valueField = (EditText) layout.findViewById(R.id.input_post_param_value);

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setView(layout);
			builder.setTitle(R.string.title_post_param_edit);
			builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if (!keyField.getText().toString().isEmpty()) {
						PostParameter parameter = new PostParameter(keyField.getText().toString(), valueField.getText().toString());
						postParameterAdapter.add(parameter);
						setListViewHeightBasedOnChildren(postParameterList);
					}
				}
			});
			builder.setNegativeButton(R.string.dialog_cancel, null);

			builder.show();
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		menu.setHeaderTitle(R.string.change_icon);

		menu.add(Menu.NONE, 2, Menu.NONE, R.string.choose_icon);
		menu.add(Menu.NONE, 0, Menu.NONE, R.string.choose_image);
		menu.add(Menu.NONE, 1, Menu.NONE, R.string.choose_ipack_icon);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case 0: // Choose an image
			// Workaround for Kitkat (thanks to http://stackoverflow.com/a/20186938/1082111)
			Intent imageIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			// intent.addCategory(Intent.CATEGORY_OPENABLE);
			// intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
			imageIntent.setType("image/*");
			startActivityForResult(imageIntent, SELECT_ICON);
			return true;
		case 1: // Choose an Ipack
			Intent iconIntent = Intent.createChooser(new Intent(IpackKeys.Actions.ICON_SELECT), getText(R.string.choose_ipack));
			startActivityForResult(iconIntent, SELECT_IPACK_ICON);
			return true;
		case 2: // Choose a built-in icon
			IconSelector iconSelector = new IconSelector(this, new OnIconSelectedListener() {

				@Override
				public void onIconSelected(String resourceName) {
					iconView.setImageResource(getResources().getIdentifier(resourceName, "drawable", getPackageName()));

					selectedIcon = resourceName;
					hasChanges = true;
				}

			});
			iconSelector.show();
			return true;
		}

		return false;
	}

	@Override
	public void onBackPressed() {
		confirmClose();
	}

	private void confirmClose() {
		if (hasChanges) {
			new AlertDialog.Builder(this).setTitle(R.string.confirm_discard_changes_title).setMessage(R.string.confirm_discard_changes_message)
					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							cancelAndClose();
						}

					}).setNegativeButton(android.R.string.no, null).show();
		} else {
			cancelAndClose();
		}
	}

	private void saveAndClose() {

		// Validation
		if (nameView.getText().toString().matches("^\\s*$")) {
			nameView.setError(getText(R.string.validation_name_not_empty));
			nameView.requestFocus();
			return;
		}
		String url = urlView.getText().toString();
		if (urlView.getText().length() == 0 || url.equalsIgnoreCase("http://") || url.equalsIgnoreCase("https://") || !(URLUtil.isHttpUrl(url) || URLUtil.isHttpsUrl(url))) {
			urlView.setError(getText(R.string.validation_url_invalid));
			urlView.requestFocus();
			return;
		}

		String protocol;
		if (URLUtil.isHttpsUrl(url)) {
			protocol = Shortcut.PROTOCOL_HTTPS;
			url = url.substring(8);
		} else {
			protocol = Shortcut.PROTOCOL_HTTP;
			url = url.substring(7);
		}

		shortcut.setURL(url);
		shortcut.setProtocol(protocol);
		shortcut.setMethod(selectedMethod);
		shortcut.setName(nameView.getText().toString().trim());
		shortcut.setDescription(descriptionView.getText().toString().trim());
		shortcut.setPassword(passwordView.getText().toString());
		shortcut.setUsername(usernameView.getText().toString());
		shortcut.setIconName(selectedIcon);
		shortcut.setFeedback(selectedFeedback);

		long shortcutID = shortcutStorage.storeShortcut(shortcut);

		Intent returnIntent = new Intent();
		returnIntent.putExtra(EXTRA_SHORTCUT_ID, shortcutID);
		setResult(RESULT_OK, returnIntent);
		finish();
	}

	private void cancelAndClose() {
		Intent returnIntent = new Intent();
		setResult(RESULT_CANCELED, returnIntent);
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
					out.flush();

					selectedIcon = iconName;
					hasChanges = true;
				} catch (Exception e) {
					e.printStackTrace();
					iconView.setImageResource(Shortcut.DEFAULT_ICON);
					selectedIcon = null;
					hasChanges = true;
					Toast.makeText(this, R.string.error_set_image, Toast.LENGTH_SHORT).show();
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
			} else if (requestCode == SELECT_IPACK_ICON && resultCode == RESULT_OK) {
				String ipackageName = intent.getData().getAuthority();
				int id = intent.getIntExtra(IpackKeys.Extras.ICON_ID, -1);
				Uri uri = Uri.parse("android.resource://" + ipackageName + "/" + id);
				iconView.setImageURI(uri);

				selectedIcon = uri.toString();
				hasChanges = true;
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		final PostParameter parameter = postParameterAdapter.getItem(position);

		LayoutInflater inflater = LayoutInflater.from(this);
		View layout = inflater.inflate(R.layout.dialog_edit_post_parameter, null);

		final EditText keyField = (EditText) layout.findViewById(R.id.input_post_param_key);
		keyField.setText(parameter.getKey());

		final EditText valueField = (EditText) layout.findViewById(R.id.input_post_param_value);
		valueField.setText(parameter.getValue());

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(layout);
		builder.setTitle(R.string.title_post_param_edit);
		builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (!keyField.getText().toString().isEmpty()) {
					parameter.setKey(keyField.getText().toString());
					parameter.setValue(valueField.getText().toString());
					postParameterAdapter.notifyDataSetChanged();
				}
			}
		});
		builder.setNeutralButton(R.string.dialog_remove, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				postParameterAdapter.remove(parameter);
				setListViewHeightBasedOnChildren(postParameterList);
			}
		});
		builder.setNegativeButton(R.string.dialog_cancel, null);

		builder.show();
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		switch (parent.getId()) {
		case R.id.input_method:
			selectedMethod = Shortcut.METHODS[position];
			if (!selectedMethod.equals(shortcut.getMethod())) {
				hasChanges = true;
			}

			if (selectedMethod == Shortcut.METHOD_GET) {
				postParamsContainer.setVisibility(View.GONE);
			} else {
				postParamsContainer.setVisibility(View.VISIBLE);
			}

			break;
		case R.id.input_feedback:
			selectedFeedback = Shortcut.FEEDBACKS[position];
			if (selectedFeedback != shortcut.getFeedback()) {
				hasChanges = true;
			}
			break;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	public String getRealPathFromURI(Context context, Uri contentUri) {
		Cursor cursor = null;
		try {
			String[] proj = { MediaStore.Images.Media.DATA };
			cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		hasChanges = true;
	}

	@Override
	public void afterTextChanged(Editable s) {
	}

	/**
	 * Method for Setting the Height of the ListView dynamically. Hack to fix the issue of not showing all the items of the ListView when placed inside a ScrollView.
	 * 
	 * @param listView
	 */
	private void setListViewHeightBasedOnChildren(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null)
			return;

		int desiredWidth = MeasureSpec.makeMeasureSpec(listView.getWidth(), MeasureSpec.UNSPECIFIED);
		int totalHeight = 0;
		View view = null;
		for (int i = 0; i < listAdapter.getCount(); i++) {
			view = listAdapter.getView(i, view, listView);
			if (i == 0)
				view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, LayoutParams.WRAP_CONTENT));

			view.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
			totalHeight += view.getMeasuredHeight();
		}
		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		listView.setLayoutParams(params);
		listView.requestLayout();
	}

}
