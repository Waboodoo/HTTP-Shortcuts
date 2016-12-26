package ch.rmy.android.http_shortcuts;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import ch.rmy.android.http_shortcuts.realm.Controller;
import ch.rmy.android.http_shortcuts.realm.models.Shortcut;
import ch.rmy.android.http_shortcuts.utils.IntentUtil;

// THIS IMPLEMENTATION IS EXPERIMENTAL ONLY
public class VoiceActivity extends BaseActivity {

    private Controller controller;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String shortcutName = getIntent().getStringExtra(SearchManager.QUERY);
        if (shortcutName == null) {
            finishWithoutAnimation();
            return;
        }

        controller = destroyer.own(new Controller(getContext()));
        Shortcut shortcut = controller.getShortcutByName(shortcutName);
        if (shortcut == null) {
            Toast.makeText(getContext(), "Shortcut \"" + shortcutName + "\" not found", Toast.LENGTH_LONG).show();
            finishWithoutAnimation();
            return;
        }

        Intent intent = IntentUtil.createIntent(getContext(), shortcut.getId());
        startActivity(intent);
        finishWithoutAnimation();
    }

    private void finishWithoutAnimation() {
        overridePendingTransition(0, 0);
        finish();
        overridePendingTransition(0, 0);
    }
}
