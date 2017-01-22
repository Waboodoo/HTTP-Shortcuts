package ch.rmy.android.http_shortcuts;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import ch.rmy.android.http_shortcuts.realm.Controller;
import ch.rmy.android.http_shortcuts.realm.models.Shortcut;
import ch.rmy.android.http_shortcuts.utils.IntentUtil;
import ch.rmy.android.http_shortcuts.utils.MenuDialogBuilder;
import ch.rmy.android.http_shortcuts.variables.VariableProvider;
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
            finishWithoutAnimation();
            return;
        }

        HashMap<String, String> variableValues = new HashMap<>();
        variableValues.put(VariableProvider.INTERNAL_VARIABLE_SHARE, text);

        Controller controller = destroyer.own(new Controller(getContext()));
        List<Shortcut> shortcuts = getTargetableShortcuts(controller);

        if (shortcuts.size() == 1) {
            executeShortcut(shortcuts.get(0), variableValues);
            finishWithoutAnimation();
        } else if (shortcuts.isEmpty()) {
            showInstructions();
        } else {
            showShortcutSelection(shortcuts, variableValues);
        }

        // TODO:
        // - Write documentation
        // - Variable input auto complete
        // - Translate
    }

    private List<Shortcut> getTargetableShortcuts(Controller controller) {
        List<Shortcut> shortcuts = new ArrayList<>();
        for (Shortcut shortcut : controller.getShortcuts()) {
            Set<String> variableNames = VariableResolver.extractVariableNames(shortcut);
            if (variableNames.contains(VariableProvider.INTERNAL_VARIABLE_SHARE)) {
                shortcuts.add(shortcut);
            }
        }
        return shortcuts;
    }

    private void executeShortcut(Shortcut shortcut, HashMap<String, String> variableValues) {
        Intent intent = IntentUtil.createIntent(getContext(), shortcut.getId(), variableValues);
        startActivity(intent);
    }

    private void showInstructions() {
        new MaterialDialog.Builder(getContext())
                .title("No suitable shortcuts found") // TODO
                .content("Foo") // TODO
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finishWithoutAnimation();
                    }
                })
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
