package ch.rmy.android.http_shortcuts.variables;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.jdeferred.Deferred;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.rmy.android.http_shortcuts.realm.models.Header;
import ch.rmy.android.http_shortcuts.realm.models.Parameter;
import ch.rmy.android.http_shortcuts.realm.models.Shortcut;
import ch.rmy.android.http_shortcuts.realm.models.Variable;

public class VariableResolver {

    private final Context context;

    public VariableResolver(Context context) {
        this.context = context;
    }

    public Promise<ResolvedVariables, Void, Void> resolve(Shortcut shortcut, List<Variable> variables) {
        final Deferred<ResolvedVariables, Void, Void> deferred = new DeferredObject<>();

        Set<String> requiredVariableNames = extractVariableNames(shortcut);
        final List<Variable> variablesToResolve = new ArrayList<>();

        for (Variable variable : variables) {
            if (requiredVariableNames.contains(variable.getKey())) {
                variablesToResolve.add(variable);
            }
        }

        final ResolvedVariables.Builder builder = new ResolvedVariables.Builder();

        //TODO: Implement UI interactions and remove this test code
        if (variablesToResolve.isEmpty()) {
            deferred.resolve(builder.build());
        } else {

            for (Variable variable : variablesToResolve) {
                builder.add(variable.getKey(), "cats");
            }

            new MaterialDialog.Builder(context)
                    .positiveText("Send")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            deferred.resolve(builder.build());
                        }
                    })
                    .dismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            if (deferred.isPending()) {
                                deferred.reject(null);
                            }
                        }
                    })
                    .show();
        }

        return deferred.promise();
    }

    private Set<String> extractVariableNames(Shortcut shortcut) {
        Set<String> discoveredVariables = new HashSet<>();

        discoveredVariables.addAll(Variables.extractVariableNames(shortcut.getUrl()));
        discoveredVariables.addAll(Variables.extractVariableNames(shortcut.getUsername()));
        discoveredVariables.addAll(Variables.extractVariableNames(shortcut.getPassword()));
        discoveredVariables.addAll(Variables.extractVariableNames(shortcut.getBodyContent()));

        if (!Shortcut.METHOD_GET.equals(shortcut.getMethod())) {
            for (Parameter parameter : shortcut.getParameters()) {
                discoveredVariables.addAll(Variables.extractVariableNames(parameter.getKey()));
                discoveredVariables.addAll(Variables.extractVariableNames(parameter.getValue()));
            }
        }
        for (Header header : shortcut.getHeaders()) {
            discoveredVariables.addAll(Variables.extractVariableNames(header.getKey()));
            discoveredVariables.addAll(Variables.extractVariableNames(header.getValue()));
        }

        return discoveredVariables;
    }

}
