package ch.rmy.android.http_shortcuts.http;

import android.app.IntentService;
import android.content.Intent;

import java.util.HashMap;

import ch.rmy.android.http_shortcuts.realm.Controller;
import ch.rmy.android.http_shortcuts.realm.models.PendingExecution;
import ch.rmy.android.http_shortcuts.realm.models.ResolvedVariable;
import ch.rmy.android.http_shortcuts.utils.Connectivity;
import ch.rmy.android.http_shortcuts.utils.IntentUtil;
import io.realm.RealmResults;

public class ExecutionService extends IntentService {

    private static final int INITIAL_DELAY = 1500;

    public ExecutionService() {
        super(ExecutionService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final Controller controller = new Controller();

        while (Connectivity.INSTANCE.isNetworkConnected(this)) {
            RealmResults<PendingExecution> pendingExecutions = controller.getShortcutsPendingExecution();
            if (pendingExecutions.isEmpty()) {
                break;
            }

            final PendingExecution pendingExecution = pendingExecutions.first();
            long id = pendingExecution.getShortcutId();
            HashMap<String, String> variableValues = new HashMap<>();
            for (ResolvedVariable resolvedVariable : pendingExecution.getResolvedVariables()) {
                variableValues.put(resolvedVariable.getKey(), resolvedVariable.getValue());
            }

            controller.removePendingExecution(pendingExecution);

            try {
                Thread.sleep(INITIAL_DELAY);
                executeShortcut(id, variableValues);
            } catch (InterruptedException e) {
                break;
            }
        }

        controller.destroy();
    }

    private void executeShortcut(long id, HashMap<String, String> variableValues) {
        Intent shortcutIntent = IntentUtil.INSTANCE.createIntent(this, id, variableValues);
        startActivity(shortcutIntent);
    }

}
