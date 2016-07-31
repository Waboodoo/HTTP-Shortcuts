package ch.rmy.android.http_shortcuts.plugin;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.twofortyfouram.locale.sdk.client.ui.activity.AbstractFragmentPluginActivity;

import ch.rmy.android.http_shortcuts.MainActivity;

public class PluginEditActivity extends AbstractFragmentPluginActivity {

    public static final String ACTION_SELECT_SHORTCUT_FOR_PLUGIN = "ch.rmy.android.http_shortcuts.plugin";
    private static final int REQUEST_SELECT = 1;

    private Bundle bundle;
    private String name;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(ACTION_SELECT_SHORTCUT_FOR_PLUGIN);

        startActivityForResult(intent, REQUEST_SELECT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT) {
            if (resultCode == RESULT_OK) {
                long id = data.getExtras().getLong(MainActivity.EXTRA_SELECTION_ID);
                bundle = PluginBundleManager.generateBundle(id);
                name = data.getExtras().getString(MainActivity.EXTRA_SELECTION_NAME);
            }
            finish();
        }
    }

    @Override
    public boolean isBundleValid(@NonNull Bundle bundle) {
        return PluginBundleManager.isBundleValid(bundle);
    }

    @Override
    public void onPostCreateWithPreviousResult(@NonNull Bundle bundle, @NonNull String s) {

    }

    @Nullable
    @Override
    public Bundle getResultBundle() {
        return bundle;
    }

    @NonNull
    @Override
    public String getResultBlurb(@NonNull Bundle bundle) {
        return name;
    }

}
