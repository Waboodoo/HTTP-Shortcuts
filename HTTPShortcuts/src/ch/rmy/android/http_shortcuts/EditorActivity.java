package ch.rmy.android.http_shortcuts;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;
import ch.rmy.android.http_shortcuts.shortcuts.Shortcut;

/**
 * The activity to create/edit shortcuts.
 * 
 * @author Roland Meyer
 */
public class EditorActivity extends Activity implements OnClickListener, OnItemSelectedListener, TextWatcher {

	public final static String EXTRA_SHORTCUT = "shortcut";
	private final static int SELECT_ICON = 1;
	public final static int EDIT_SHORTCUT = 2;

	private Shortcut shortcut;

	private EditText nameView;
	private EditText urlView;
	private EditText usernameView;
	private EditText passwordView;
	private ImageView iconView;
	private Spinner methodView;
	private Spinner feedbackView;
	private Button button;

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

		shortcut = (Shortcut) getIntent().getParcelableExtra(EXTRA_SHORTCUT);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			window.setStatusBarColor(getResources().getColor(R.color.dark_blue));
		}

		nameView = (EditText) findViewById(R.id.input_shortcut_name);
		urlView = (EditText) findViewById(R.id.input_url);
		usernameView = (EditText) findViewById(R.id.input_username);
		passwordView = (EditText) findViewById(R.id.input_password);
		iconView = (ImageView) findViewById(R.id.input_icon);
		button = (Button) findViewById(R.id.create_button);

		nameView.setText(shortcut.getName());
		urlView.setText(shortcut.getProtocol() + "://" + shortcut.getURL());
		usernameView.setText(shortcut.getUsername());
		passwordView.setText(shortcut.getPassword());

		nameView.addTextChangedListener(this);
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

		if (shortcut.getIconName() != null) {
			iconView.setImageURI(Uri.fromFile(getFileStreamPath(shortcut.getIconName())));
		} else {
			iconView.setImageResource(Shortcut.DEFAULT_ICON);
		}
		iconView.setOnClickListener(this);
		selectedIcon = shortcut.getIconName();

		if (shortcut.isNew()) {
			getActionBar().setTitle(R.string.create_shortcut);
			button.setText(R.string.create_button);
		} else {
			getActionBar().setTitle(R.string.edit_shortcut);
			button.setText(R.string.save_button);
		}
		button.setOnClickListener(this);

		hasChanges = false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			confirmClose();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		if (v.equals(button)) {
			saveAndClose();
		} else if (v.equals(iconView)) {
			// Workaround for Kitkat (thanks to http://stackoverflow.com/a/20186938/1082111)
			Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			// intent.addCategory(Intent.CATEGORY_OPENABLE);
			// intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
			intent.setType("image/*");
			startActivityForResult(intent, SELECT_ICON);
		}
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
		if (urlView.getText().length() == 0 || !(URLUtil.isHttpUrl(url) || URLUtil.isHttpsUrl(url))) {
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
		shortcut.setPassword(passwordView.getText().toString());
		shortcut.setUsername(usernameView.getText().toString());
		shortcut.setIconName(selectedIcon);
		shortcut.setFeedback(selectedFeedback);

		Intent returnIntent = new Intent();
		returnIntent.putExtra(EXTRA_SHORTCUT, shortcut);
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

		if (requestCode == SELECT_ICON && resultCode == RESULT_OK) {
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
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		switch (parent.getId()) {
		case R.id.input_method:
			selectedMethod = Shortcut.METHODS[position];
			if (!selectedMethod.equals(shortcut.getMethod())) {
				hasChanges = true;
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

}
