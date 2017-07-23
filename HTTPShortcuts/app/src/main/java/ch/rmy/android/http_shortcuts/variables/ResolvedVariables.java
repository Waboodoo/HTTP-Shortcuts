package ch.rmy.android.http_shortcuts.variables;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.rmy.android.http_shortcuts.realm.models.ResolvedVariable;
import ch.rmy.android.http_shortcuts.realm.models.Variable;

public class ResolvedVariables {

    private final Map<String, String> variableValues = new HashMap<>();

    public boolean hasValue(String variableName) {
        return variableValues.containsKey(variableName);
    }

    public String getValue(String variableName) {
        return variableValues.get(variableName);
    }

    public List<ResolvedVariable> toList() {
        List<ResolvedVariable> resolvedVariables = new ArrayList<>();
        for (Map.Entry<String, String> entries : variableValues.entrySet()) {
            resolvedVariables.add(ResolvedVariable.Companion.createNew(entries.getKey(), entries.getValue()));
        }
        return resolvedVariables;
    }

    public static class Builder {

        private final ResolvedVariables resolvedVariables;

        protected Builder() {
            resolvedVariables = new ResolvedVariables();
        }

        protected Builder add(Variable variable, String value) {
            if (variable.getJsonEncode()) {
                value = JSONObject.quote(value);
                value = value.substring(1, value.length() - 1);
            }
            if (variable.getUrlEncode()) {
                try {
                    value = URLEncoder.encode(value, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    // what kind of stupid system does not support utf-8?!
                }
            }
            resolvedVariables.variableValues.put(variable.getKey(), value);
            return this;
        }

        protected ResolvedVariables build() {
            return resolvedVariables;
        }

    }

}
