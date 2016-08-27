package ch.rmy.android.http_shortcuts.variables;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import ch.rmy.android.http_shortcuts.realm.models.Variable;

public class ResolvedVariables {

    private final Map<String, String> variableValues = new HashMap<>();

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

        protected Builder add(Variable variable, String value) {
            if (variable.isJsonEncode()) {
                value = JSONObject.quote(value);
                value = value.substring(1, value.length()-1);
            }
            if (variable.isUrlEncode()) {
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
