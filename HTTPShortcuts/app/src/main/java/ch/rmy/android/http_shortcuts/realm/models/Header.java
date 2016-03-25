package ch.rmy.android.http_shortcuts.realm.models;

import ch.rmy.android.http_shortcuts.key_value_pairs.KeyValuePair;
import io.realm.RealmObject;

public class Header extends RealmObject implements KeyValuePair {

    private String key;
    private String value;

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
