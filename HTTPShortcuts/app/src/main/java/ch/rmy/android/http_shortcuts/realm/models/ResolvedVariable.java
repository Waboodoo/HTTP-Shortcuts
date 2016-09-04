package ch.rmy.android.http_shortcuts.realm.models;

import io.realm.RealmObject;

public class ResolvedVariable extends RealmObject {

    private String key;
    private String value;

    public static ResolvedVariable createNew(String key, String value) {
        ResolvedVariable resolvedVariable = new ResolvedVariable();
        resolvedVariable.setKey(key);
        resolvedVariable.setValue(value);
        return resolvedVariable;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
