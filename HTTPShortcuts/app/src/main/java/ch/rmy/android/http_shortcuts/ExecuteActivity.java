package ch.rmy.android.http_shortcuts;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.VolleyError;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import ch.rmy.android.http_shortcuts.http.HttpRequester;
import ch.rmy.android.http_shortcuts.http.Response;
import ch.rmy.android.http_shortcuts.realm.Controller;
import ch.rmy.android.http_shortcuts.realm.models.Shortcut;
import ch.rmy.android.http_shortcuts.realm.models.Variable;
import ch.rmy.android.http_shortcuts.variables.ResolvedVariables;
import ch.rmy.android.http_shortcuts.variables.VariableResolver;

public class ExecuteActivity extends BaseActivity {

    public static final String ACTION_EXECUTE_SHORTCUT = "ch.rmy.android.http_shortcuts.execute";
    public static final String EXTRA_SHORTCUT_ID = "id";
    public static final String EXTRA_VARIABLE_VALUES = "variable_values";

    private static final int TOAST_MAX_LENGTH = 400;

    private Controller controller;

    @Bind(R.id.response_text)
    TextView responseText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        long shortcutId = getShortcutId(getIntent());
        Map<String, String> variableValues = getVariableValues(getIntent());

        controller = destroyer.own(new Controller(getContext()));
        final Shortcut shortcut = controller.getShortcutById(shortcutId);

        if (shortcut == null) {
            showToast(getString(R.string.shortcut_not_found), Toast.LENGTH_LONG);
            finishWithoutAnimation();
            return;
        }

        if (shortcut.feedbackUsesUI()) {
            switch (shortcut.getFeedback()) {
                case Shortcut.FEEDBACK_ACTIVITY: {
                    setTheme(R.style.LightTheme);
                    setContentView(R.layout.activity_execute);
                    break;
                }
            }
        }

        Promise promise = execute(shortcut, variableValues);

        if (promise.isPending()) {
            promise.done(new DoneCallback() {
                @Override
                public void onDone(Object result) {
                    if (!shortcut.feedbackUsesUI()) {
                        finishWithoutAnimation();
                    }
                }
            }).fail(new FailCallback() {
                @Override
                public void onFail(Object result) {
                    finishWithoutAnimation();
                }
            });
        } else {
            if (!shortcut.feedbackUsesUI()) {
                finishWithoutAnimation();
            }
        }
    }

    private static long getShortcutId(Intent intent) {
        long shortcutId = -1;
        Uri uri = intent.getData();
        if (uri != null) {
            try {
                String id = uri.getLastPathSegment();
                shortcutId = Long.parseLong(id);
            } catch (NumberFormatException e) {
            }
        }
        if (shortcutId == -1) {
            return intent.getLongExtra(EXTRA_SHORTCUT_ID, -1); // for backwards compatibility
        }
        return shortcutId;
    }

    private static Map<String, String> getVariableValues(Intent intent) {
        Serializable serializable = intent.getSerializableExtra(EXTRA_VARIABLE_VALUES);
        if (serializable instanceof Map) {
            return (Map<String, String>) serializable;
        }
        return new HashMap<>();
    }

    public Promise execute(final Shortcut shortcut, Map<String, String> variableValues) {
        List<Variable> variables = controller.getVariables();
        return new VariableResolver(this)
                .resolve(shortcut, variables, variableValues)
                .done(new DoneCallback<ResolvedVariables>() {
                    @Override
                    public void onDone(final ResolvedVariables resolvedVariables) {
                        HttpRequester.executeShortcut(getContext(), shortcut, resolvedVariables).done(new DoneCallback<Response>() {
                            @Override
                            public void onDone(Response response) {
                                handleSuccess(shortcut, response);
                            }
                        }).fail(new FailCallback<VolleyError>() {
                            @Override
                            public void onFail(VolleyError error) {
                                if (Shortcut.RETRY_POLICY_WAIT_FOR_INTERNET.equals(shortcut.getRetryPolicy()) && error.networkResponse == null) {
                                    controller.createPendingExecution(shortcut, resolvedVariables.toList());
                                    if (!Shortcut.FEEDBACK_NONE.equals(shortcut.getFeedback())) {
                                        showToast(String.format(getContext().getString(R.string.execution_delayed), shortcut.getSafeName(getContext())), Toast.LENGTH_LONG);
                                    }
                                } else {
                                    handleFail(shortcut, error);
                                }
                            }
                        });
                    }
                });
    }

    private void handleSuccess(Shortcut shortcut, Response response) {
        switch (shortcut.getFeedback()) {
            case Shortcut.FEEDBACK_TOAST_SIMPLE: {
                showToast(String.format(getString(R.string.executed), shortcut.getSafeName(getContext())), Toast.LENGTH_SHORT);
                break;
            }
            case Shortcut.FEEDBACK_TOAST: {
                showToast(truncateIfNeeded(response.getBody(), TOAST_MAX_LENGTH), Toast.LENGTH_LONG);
                break;
            }
            case Shortcut.FEEDBACK_DIALOG: {
                new MaterialDialog.Builder(getContext())
                        .title(shortcut.getName())
                        .content(response.getBody())
                        .positiveText(R.string.button_ok)
                        .dismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                finishWithoutAnimation();
                            }
                        })
                        .show();
                break;
            }
            case Shortcut.FEEDBACK_ACTIVITY: {
                responseText.setText(response.getBody());
                break;
            }
        }
    }

    private static String truncateIfNeeded(String string, int maxLength) {
        return string.length() > maxLength ? string.substring(0, maxLength) + "…" : string;
    }

    private void handleFail(Shortcut shortcut, VolleyError error) {
        if (Shortcut.FEEDBACK_NONE.equals(shortcut.getFeedback())) {
            return;
        }

        String name = shortcut.getSafeName(getContext());

        String message;
        if (error.networkResponse != null) {
            message = String.format(getString(R.string.error_http), name, error.networkResponse.statusCode);
        } else {
            if (error.getCause() != null && error.getCause().getMessage() != null) {
                message = String.format(getString(R.string.error_other), name, error.getCause().getMessage());
            } else if (error.getMessage() != null) {
                message = String.format(getString(R.string.error_other), name, error.getMessage());
            } else {
                message = String.format(getString(R.string.error_other), name, error.getClass().getSimpleName());
            }
            error.printStackTrace();
        }
        showToast(message, Toast.LENGTH_LONG);
    }

    private void showToast(String message, int duration) {
        Toast.makeText(getContext(), message, duration).show();
    }

    private void finishWithoutAnimation() {
        overridePendingTransition(0, 0);
        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    protected int getNavigateUpIcon() {
        return R.drawable.ic_clear;
    }

}
