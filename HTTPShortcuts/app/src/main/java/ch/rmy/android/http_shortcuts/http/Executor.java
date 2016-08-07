package ch.rmy.android.http_shortcuts.http;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.jdeferred.AlwaysCallback;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;

import java.util.List;

import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.realm.Controller;
import ch.rmy.android.http_shortcuts.realm.models.Shortcut;
import ch.rmy.android.http_shortcuts.realm.models.Variable;
import ch.rmy.android.http_shortcuts.variables.ResolvedVariables;
import ch.rmy.android.http_shortcuts.variables.VariableResolver;

public class Executor {

    private final Context context;
    private final ResponseHandler responseHandler;

    public Executor(Context context) {
        this.context = context;
        this.responseHandler = new ResponseHandler(context);
    }

    public void execute(final long shortcutId) {
        final Controller controller = new Controller(context);

        final Shortcut shortcut = controller.getShortcutById(shortcutId);
        if (shortcut == null) {
            Toast.makeText(context, R.string.shortcut_not_found, Toast.LENGTH_LONG).show();
            controller.destroy();
            return;
        }

        List<Variable> variables = controller.getVariables();
        new VariableResolver(context).resolve(shortcut, variables).done(new DoneCallback<ResolvedVariables>() {
            @Override
            public void onDone(ResolvedVariables resolvedVariables) {
                HttpRequester.executeShortcut(context, shortcut, resolvedVariables).done(new DoneCallback<String>() {
                    @Override
                    public void onDone(String response) {
                        responseHandler.handleSuccess(shortcut, response);
                    }
                }).fail(new FailCallback<VolleyError>() {
                    @Override
                    public void onFail(VolleyError error) {
                        if (Shortcut.RETRY_POLICY_WAIT_FOR_INTERNET.equals(shortcut.getRetryPolicy()) && error.networkResponse == null) {
                            controller.createPendingExecution(shortcut);
                            if (!Shortcut.FEEDBACK_NONE.equals(shortcut.getFeedback())) {
                                Toast.makeText(context, String.format(context.getText(R.string.execution_delayed).toString(), shortcut.getSafeName(context)), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            responseHandler.handleFailure(shortcut, error);
                        }
                    }
                }).always(new AlwaysCallback<String, VolleyError>() {
                    @Override
                    public void onAlways(Promise.State state, String resolved, VolleyError rejected) {
                        controller.destroy();
                    }
                });
            }
        }).fail(new FailCallback<Throwable>() {
            @Override
            public void onFail(Throwable result) {
                controller.destroy();
            }
        });
    }

}
