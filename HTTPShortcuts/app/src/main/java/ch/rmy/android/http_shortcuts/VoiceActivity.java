package ch.rmy.android.http_shortcuts;

import android.app.Activity;
import android.app.SearchManager;
import android.os.Bundle;

import org.jdeferred.AlwaysCallback;
import org.jdeferred.Promise;


import ch.rmy.android.http_shortcuts.http.Executor;;


public class VoiceActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        String shortcutName = getIntent().getStringExtra(SearchManager.QUERY);

        Executor executor = new Executor(this);
        Promise<Void, Void, Void> promise = executor.execute(shortcutName);
        if (promise.isPending()) {
            promise.always(new AlwaysCallback<Void, Void>() {
                @Override
                public void onAlways(Promise.State state, Void resolved, Void rejected) {
                    finishWithoutAnimation();
                }
            });
        } else {
            finishWithoutAnimation();
        }
    }

    private void finishWithoutAnimation() {
        finish();
        overridePendingTransition(0, 0);
    }
}



