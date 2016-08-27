package ch.rmy.android.http_shortcuts.variables;

import android.content.Context;

import org.jdeferred.AlwaysCallback;
import org.jdeferred.Deferred;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.rmy.android.http_shortcuts.realm.Controller;
import ch.rmy.android.http_shortcuts.realm.models.Header;
import ch.rmy.android.http_shortcuts.realm.models.Parameter;
import ch.rmy.android.http_shortcuts.realm.models.Shortcut;
import ch.rmy.android.http_shortcuts.realm.models.Variable;
import ch.rmy.android.http_shortcuts.variables.types.AsyncVariableType;
import ch.rmy.android.http_shortcuts.variables.types.BaseVariableType;
import ch.rmy.android.http_shortcuts.variables.types.SyncVariableType;
import ch.rmy.android.http_shortcuts.variables.types.TypeFactory;

public class VariableResolver {

    private final Context context;

    public VariableResolver(Context context) {
        this.context = context;
    }

    public Promise<ResolvedVariables, Void, Void> resolve(Shortcut shortcut, List<Variable> variables) {
        final Deferred<ResolvedVariables, Void, Void> deferred = new DeferredObject<>();
        final Controller controller = new Controller(context);

        Set<String> requiredVariableNames = extractVariableNames(shortcut);
        final List<Variable> variablesToResolve = new ArrayList<>();

        for (Variable variable : variables) {
            if (requiredVariableNames.contains(variable.getKey())) {
                variablesToResolve.add(variable);
            }
        }

        final ResolvedVariables.Builder builder = new ResolvedVariables.Builder();

        boolean async = false;
        for (Variable variable : variablesToResolve) {
            BaseVariableType variableType = TypeFactory.getType(variable.getType());

            if (variableType instanceof AsyncVariableType) {
                async = true;
                //TODO
            }
            if (variableType instanceof SyncVariableType) {
                final String value = ((SyncVariableType) variableType).resolveValue(controller, variable);
                builder.add(variable.getKey(), value);
            }
        }

        if (async) {
            // TODO: Show UI
        } else {
            deferred.resolve(builder.build());
        }

        return deferred.promise().always(new AlwaysCallback<ResolvedVariables, Void>() {
            @Override
            public void onAlways(Promise.State state, ResolvedVariables resolved, Void rejected) {
                controller.destroy();
            }
        });
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
