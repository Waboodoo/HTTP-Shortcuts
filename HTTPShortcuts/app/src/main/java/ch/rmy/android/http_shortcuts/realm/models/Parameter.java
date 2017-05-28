package ch.rmy.android.http_shortcuts.realm.models;

import ch.rmy.android.http_shortcuts.key_value_pairs.KeyValuePair;
import ch.rmy.android.http_shortcuts.utils.UUIDUtils;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Parameter extends RealmObject implements KeyValuePair {

    @PrimaryKey
    private String id;

    @Required
    private String key;
    @Required
    private String value;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    public static Parameter createNew(String key, String value) {
        Parameter parameter = new Parameter();
        parameter.setId(UUIDUtils.create());
        parameter.setKey(key);
        parameter.setValue(value);
        return parameter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Parameter parameter = (Parameter) o;

        if (!getKey().equals(parameter.getKey())) return false;
        if (!getId().equals(parameter.getId())) return false;
        return getValue().equals(parameter.getValue());

    }

    @Override
    public int hashCode() {
        int result = getKey().hashCode();
        result = 31 * result + getValue().hashCode();
        result = 31 * result + getId().hashCode();
        return result;
    }

}
