package ch.rmy.android.http_shortcuts;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.HashMap;

import ch.rmy.android.http_shortcuts.utils.IntentUtil;

public class ShareActivity extends BaseActivity {

    private static final String TYPE_TEXT = "text/plain";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String type = getIntent().getType();
        if (!TYPE_TEXT.equals(type)) {
            finishWithoutAnimation();
            return;
        }
        String text = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        if (text == null) {
            finishWithoutAnimation();
            return;
        }

        HashMap<String, String> variableValues = new HashMap<>();
        variableValues.put("share", text); // TODO: Move this into a constant somewhere

        // TODO:
        // - Scan all shortcuts and find the ones that use the {{share}} variable
        // - If there are none, quit with an error
        // - If there are multiple, display a select dialog
        // - Write documentation
        // - Variable input auto complete
        // - Make sure {{share}} variable is always present and cannot be modified (invisible in editor?)

        Intent intent = IntentUtil.createIntent(getContext(), 7, variableValues);
        startActivity(intent);
        finishWithoutAnimation();
    }

}
