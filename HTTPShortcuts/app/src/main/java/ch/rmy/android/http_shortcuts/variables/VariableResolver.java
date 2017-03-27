package ch.rmy.android.http_shortcuts.variables;

import android.content.Context;
import android.support.annotation.Nullable;

import org.jdeferred.AlwaysCallback;
import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

    public Promise<ResolvedVariables, Void, Void> resolve(Shortcut shortcut, List<Variable> variables, @Nullable Map<String, String> preResolvedValues) {
        Set<String> requiredVariableNames = extractVariableKeys(shortcut);
        List<Variable> variablesToResolve = filterVariablesByName(variables, requiredVariableNames);
        return resolveVariables(variablesToResolve, preResolvedValues);
    }

    private List<Variable> filterVariablesByName(List<Variable> variables, Collection<String> variableNames) {
        final List<Variable> filteredVariables = new ArrayList<>();
        for (Variable variable : variables) {
            if (variableNames.contains(variable.getKey())) {
                filteredVariables.add(variable);
            }
        }
        return filteredVariables;
    }

    private Promise<ResolvedVariables, Void, Void> resolveVariables(final List<Variable> variablesToResolve, @Nullable Map<String, String> preResolvedValues) {
        final Controller controller = new Controller();
        final Deferred<ResolvedVariables, Void, Void> deferred = new DeferredObject<>();
        final ResolvedVariables.Builder builder = new ResolvedVariables.Builder();

        final List<Showable> waitingDialogs = new ArrayList<>();
        int i = 0;
        for (final Variable variable : variablesToResolve) {
            if (preResolvedValues != null && preResolvedValues.containsKey(variable.getKey())) {
                builder.add(variable, preResolvedValues.get(variable.getKey()));
                continue;
            }

            BaseVariableType variableType = TypeFactory.getType(variable.getType());

            if (variableType instanceof AsyncVariableType) {
                final int index = i++;

                final Deferred<String, Void, Void> deferredValue = new DeferredObject<>();
                deferredValue.done(new DoneCallback<String>() {
                    @Override
                    public void onDone(String result) {
                        builder.add(variable, result);

                        if (index + 1 >= waitingDialogs.size()) {
                            deferred.resolve(builder.build());
                        } else {
                            waitingDialogs.get(index + 1).show();
                        }
                    }
                }).fail(new FailCallback<Void>() {
                    @Override
                    public void onFail(Void result) {
                        deferred.reject(null);
                    }
                });

                Showable dialog = ((AsyncVariableType) variableType).createDialog(context, controller, variable, deferredValue);

                waitingDialogs.add(dialog);
            } else if (variableType instanceof SyncVariableType) {
                final String value = ((SyncVariableType) variableType).resolveValue(controller, variable);
                builder.add(variable, value);
            }
        }

        if (waitingDialogs.isEmpty()) {
            deferred.resolve(builder.build());
        } else {
            waitingDialogs.get(0).show();
        }

        return deferred.promise().always(new AlwaysCallback<ResolvedVariables, Void>() {
            @Override
            public void onAlways(Promise.State state, ResolvedVariables resolved, Void rejected) {
                resetVariableValues(controller, variablesToResolve);
                controller.destroy();
            }
        });
    }

    private void resetVariableValues(Controller controller, List<Variable> variables) {
        for (Variable variable : variables) {
            if (variable.isResetAfterUse()) {
                controller.setVariableValue(variable, "");
            }
        }
    }

    public static Set<String> extractVariableKeys(Shortcut shortcut) {
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
