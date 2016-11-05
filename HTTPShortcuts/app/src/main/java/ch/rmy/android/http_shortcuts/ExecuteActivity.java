package ch.rmy.android.http_shortcuts;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.VolleyError;

import org.jdeferred.AlwaysCallback;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;

import java.util.List;
import java.util.Map;

import butterknife.Bind;
import ch.rmy.android.http_shortcuts.http.HttpRequester;
import ch.rmy.android.http_shortcuts.http.Response;
import ch.rmy.android.http_shortcuts.realm.Controller;
import ch.rmy.android.http_shortcuts.realm.models.Shortcut;
import ch.rmy.android.http_shortcuts.realm.models.Variable;
import ch.rmy.android.http_shortcuts.utils.IntentUtil;
import ch.rmy.android.http_shortcuts.variables.ResolvedVariables;
import ch.rmy.android.http_shortcuts.variables.VariableResolver;
import fr.castorflex.android.circularprogressbar.CircularProgressBar;

public class ExecuteActivity extends BaseActivity {

    public static final String ACTION_EXECUTE_SHORTCUT = "ch.rmy.android.http_shortcuts.resolveVariablesAndExecute";
    public static final String EXTRA_SHORTCUT_ID = "id";
    public static final String EXTRA_VARIABLE_VALUES = "variable_values";

    private static final int TOAST_MAX_LENGTH = 400;

    private Controller controller;
    private Shortcut shortcut;
    private Response lastResponse;

    private ProgressDialog progressDialog;

    @Bind(R.id.response_text)
    TextView responseText;
    @Bind(R.id.progress_spinner)
    CircularProgressBar progressSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        long shortcutId = IntentUtil.getShortcutId(getIntent());
        Map<String, String> variableValues = IntentUtil.getVariableValues(getIntent());

        controller = new Controller(getContext());
        shortcut = controller.getShortcutById(shortcutId);

        if (shortcut == null) {
            showToast(getString(R.string.shortcut_not_found), Toast.LENGTH_LONG);
            controller.destroy();
            finishWithoutAnimation();
            return;
        }
        setTitle(shortcut.getName());

        if (Shortcut.FEEDBACK_ACTIVITY.equals(shortcut.getFeedback())) {
            setTheme(R.style.LightTheme);
            setContentView(R.layout.activity_execute);
        }

        Promise promise = resolveVariablesAndExecute(variableValues);

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

    public Promise resolveVariablesAndExecute(Map<String, String> variableValues) {
        List<Variable> variables = controller.getVariables();
        return new VariableResolver(this)
                .resolve(shortcut, variables, variableValues)
                .done(new DoneCallback<ResolvedVariables>() {
                    @Override
                    public void onDone(ResolvedVariables resolvedVariables) {
                        execute(resolvedVariables);
                    }
                }).fail(new FailCallback<Void>() {
                    @Override
                    public void onFail(Void result) {
                        controller.destroy();
                    }
                });
    }

    private void execute(final ResolvedVariables resolvedVariables) {
        showProgress();
        HttpRequester.executeShortcut(getContext(), shortcut, resolvedVariables).done(new DoneCallback<Response>() {
            @Override
            public void onDone(Response response) {
                setLastResponse(response);
                displayOutput(generateOutputFromResponse(response));
            }
        }).fail(new FailCallback<VolleyError>() {
            @Override
            public void onFail(VolleyError error) {
                if (Shortcut.RETRY_POLICY_WAIT_FOR_INTERNET.equals(shortcut.getRetryPolicy()) && error.networkResponse == null) {
                    controller.createPendingExecution(shortcut, resolvedVariables.toList());
                    if (!Shortcut.FEEDBACK_NONE.equals(shortcut.getFeedback())) {
                        showToast(String.format(getContext().getString(R.string.execution_delayed), shortcut.getSafeName(getContext())), Toast.LENGTH_LONG);
                    }
                    finishWithoutAnimation();
                } else {
                    setLastResponse(null);
                    displayOutput(generateOutputFromError(error));
                }
            }
        }).always(new AlwaysCallback<Response, VolleyError>() {
            @Override
            public void onAlways(Promise.State state, Response resolved, VolleyError rejected) {
                hideProgress();
                controller.destroy();
            }
        });
    }

    private String generateOutputFromResponse(Response response) {
        return response.getBody();
    }

    private String generateOutputFromError(VolleyError error) {
        String name = shortcut.getSafeName(getContext());

        if (error.networkResponse != null) {
            StringBuilder builder = new StringBuilder();
            builder.append(String.format(getString(R.string.error_http), name, error.networkResponse.statusCode));

            if (error.networkResponse.data != null) {
                try {
                    builder.append("\n");
                    builder.append("\n");
                    builder.append(new String(error.networkResponse.data));
                } catch (Exception e) {

                }
            }

            return builder.toString();
        } else {
            if (error.getCause() != null && error.getCause().getMessage() != null) {
                return String.format(getString(R.string.error_other), name, error.getCause().getMessage());
            } else if (error.getMessage() != null) {
                return String.format(getString(R.string.error_other), name, error.getMessage());
            } else {
                return String.format(getString(R.string.error_other), name, error.getClass().getSimpleName());
            }
        }
    }

    private void showProgress() {
        switch (shortcut.getFeedback()) {
            case Shortcut.FEEDBACK_DIALOG: {
                if (progressDialog == null) {
                    progressDialog = ProgressDialog.show(getContext(), null, String.format(getString(R.string.progress_dialog_message), shortcut.getName()));
                }
                break;
            }
            case Shortcut.FEEDBACK_ACTIVITY: {
                progressSpinner.setVisibility(View.VISIBLE);
                responseText.setVisibility(View.GONE);
                break;
            }
        }
    }

    private void hideProgress() {
        switch (shortcut.getFeedback()) {
            case Shortcut.FEEDBACK_DIALOG: {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
                break;
            }
            case Shortcut.FEEDBACK_ACTIVITY: {
                progressSpinner.setVisibility(View.GONE);
                responseText.setVisibility(View.VISIBLE);
                break;
            }
        }
    }

    private void displayOutput(String output) {
        switch (shortcut.getFeedback()) {
            case Shortcut.FEEDBACK_TOAST_SIMPLE: {
                showToast(String.format(getString(R.string.executed), shortcut.getSafeName(getContext())), Toast.LENGTH_SHORT);
                break;
            }
            case Shortcut.FEEDBACK_TOAST: {
                showToast(truncateIfNeeded(output, TOAST_MAX_LENGTH), Toast.LENGTH_LONG);
                break;
            }
            case Shortcut.FEEDBACK_DIALOG: {
                new MaterialDialog.Builder(getContext())
                        .title(shortcut.getName())
                        .content(output)
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
                responseText.setText(output);
                break;
            }
        }
    }

    private static String truncateIfNeeded(String string, int maxLength) {
        return string.length() > maxLength ? string.substring(0, maxLength) + "…" : string;
    }

    private void showToast(String message, int duration) {
        Toast.makeText(getContext(), message, duration).show();
    }

    private void setLastResponse(Response response) {
        this.lastResponse = response;
        invalidateOptionsMenu();
    }

    private void finishWithoutAnimation() {
        hideProgress();
        overridePendingTransition(0, 0);
        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.execute_activity_menu, menu);
        menu.findItem(R.id.action_share_response).setVisible(lastResponse != null);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_share_response) {
            shareLastResponse();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareLastResponse() {
        if (lastResponse == null) {
            return;
        }
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, lastResponse.getBody());
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_title)));
    }

    @Override
    protected int getNavigateUpIcon() {
        return R.drawable.ic_clear;
    }

}
