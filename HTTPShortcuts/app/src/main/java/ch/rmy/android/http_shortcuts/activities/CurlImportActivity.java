package ch.rmy.android.http_shortcuts.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import butterknife.Bind;
import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.utils.SimpleTextWatcher;
import ch.rmy.curlcommand.CurlCommand;
import ch.rmy.curlcommand.CurlParser;

public class CurlImportActivity extends BaseActivity {

    private static final int REQUEST_CREATE_SHORTCUT = 1;

    private boolean commandEmpty = true;

    @Bind(R.id.curl_import_command)
    EditText curlCommand;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curl_import);

        curlCommand.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                checkIfCommandEmpty();
            }
        });
    }

    private void checkIfCommandEmpty() {
        boolean commandEmpty = curlCommand.getText().length() == 0;
        if (this.commandEmpty != commandEmpty) {
            this.commandEmpty = commandEmpty;
            invalidateOptionsMenu();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.curl_import_activity_menu, menu);
        menu.findItem(R.id.action_create_from_curl).setVisible(curlCommand.getText().length() > 0);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /*case android.R.id.home: {

                return true;
            }*/
            case R.id.action_create_from_curl: {
                startImport();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void startImport() {
        String commandString = curlCommand.getText().toString();
        CurlCommand command = CurlParser.parse(commandString);

        Intent intent = new Intent(this, EditorActivity.class);
        intent.putExtra(EditorActivity.EXTRA_CURL_COMMAND, command);
        startActivityForResult(intent, REQUEST_CREATE_SHORTCUT);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_CREATE_SHORTCUT) {
            long shortcutId = intent.getLongExtra(EditorActivity.EXTRA_SHORTCUT_ID, 0);
            Intent returnIntent = new Intent();
            returnIntent.putExtra(EditorActivity.EXTRA_SHORTCUT_ID, shortcutId);
            setResult(RESULT_OK, returnIntent);
        }
        finish();
    }

    @Override
    protected int getNavigateUpIcon() {
        return R.drawable.ic_clear;
    }

}
