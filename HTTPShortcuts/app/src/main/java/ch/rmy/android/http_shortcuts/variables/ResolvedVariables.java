package ch.rmy.android.http_shortcuts.variables;

import java.util.HashMap;
import java.util.Map;

public class ResolvedVariables {

    private final Map<String, String> variableValues = new HashMap<>();

    protected ResolvedVariables() {

    }

    public boolean hasValue(String variableName) {
        return variableValues.containsKey(variableName);
    }

    public String getValue(String variableName) {
        return variableValues.get(variableName);
    }

    public static class Builder {

        private final ResolvedVariables resolvedVariables;

        protected Builder() {
            resolvedVariables = new ResolvedVariables();
        }

        protected Builder add(String key, String value) {
            resolvedVariables.variableValues.put(key, value);
            return this;
        }

        protected ResolvedVariables build() {
            return resolvedVariables;
        }

    }

}
