package ch.rmy.android.http_shortcuts;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.rmy.android.http_shortcuts.realm.Controller;
import ch.rmy.android.http_shortcuts.realm.models.Shortcut;
import ch.rmy.android.http_shortcuts.realm.models.Variable;
import ch.rmy.android.http_shortcuts.utils.IntentUtil;
import ch.rmy.android.http_shortcuts.utils.MenuDialogBuilder;
import ch.rmy.android.http_shortcuts.variables.VariableResolver;

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
            showInstructions(R.string.error_sharing_feature_not_available_yet);
            return;
        }

        Controller controller = destroyer.own(new Controller(getContext()));
        Set<String> variables = getTargetableVariables(controller);
        List<Shortcut> shortcuts = getTargetableShortcuts(controller, variables);

        HashMap<String, String> variableValues = new HashMap<>();
        for (String variable : variables) {
            variableValues.put(variable, text);
        }

        if (shortcuts.size() == 1) {
            executeShortcut(shortcuts.get(0), variableValues);
            finishWithoutAnimation();
        } else if (shortcuts.isEmpty()) {
            showInstructions(R.string.error_not_suitable_shortcuts);
        } else {
            showShortcutSelection(shortcuts, variableValues);
        }

        // TODO:
        // - Write documentation
        // - Variable input auto complete
        // - Translate
    }

    private Set<String> getTargetableVariables(Controller controller) {
        Set<String> targetableVariables = new HashSet<>();
        List<Variable> variables = controller.getVariables();
        for (Variable variable : variables) {
            if (variable.isShareText()) {
                targetableVariables.add(variable.getKey());
            }
        }
        return targetableVariables;
    }

    private List<Shortcut> getTargetableShortcuts(Controller controller, Set<String> variableKeys) {
        List<Shortcut> shortcuts = new ArrayList<>();
        for (Shortcut shortcut : controller.getShortcuts()) {
            if (hasShareVariable(shortcut, variableKeys)) {
                shortcuts.add(shortcut);
            }
        }
        return shortcuts;
    }

    private boolean hasShareVariable(Shortcut shortcut, Set<String> variableKeys) {
        Set<String> variableKeysInShortcut = VariableResolver.extractVariableKeys(shortcut);
        for (String key : variableKeys) {
            if (variableKeysInShortcut.contains(key)) {
                return true;
            }
        }
        return false;
    }

    private void executeShortcut(Shortcut shortcut, HashMap<String, String> variableValues) {
        Intent intent = IntentUtil.createIntent(getContext(), shortcut.getId(), variableValues);
        startActivity(intent);
    }

    private void showInstructions(@StringRes int text) {
        new MaterialDialog.Builder(getContext())
                .content(text)
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finishWithoutAnimation();
                    }
                })
                .positiveText(R.string.button_ok)
                .show();
    }

    private void showShortcutSelection(List<Shortcut> shortcuts, final HashMap<String, String> variableValues) {
        MenuDialogBuilder builder = new MenuDialogBuilder(getContext());
        for (final Shortcut shortcut : shortcuts) {
            builder.item(shortcut.getName(), new MenuDialogBuilder.Action() {
                @Override
                public void execute() {
                    executeShortcut(shortcut, variableValues);
                }
            });
        }
        builder.dismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                finishWithoutAnimation();
            }
        }).show();
    }

}
