package ch.rmy.android.http_shortcuts.realm.models;

import ch.rmy.android.http_shortcuts.utils.UUIDUtils;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Option extends RealmObject {

    @PrimaryKey
    private String id;

    private String label;
    private String value;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static Option createNew(String label, String value) {
        Option option = new Option();
        option.setId(UUIDUtils.create());
        option.setLabel(label);
        option.setValue(value);
        return option;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Option option = (Option) o;

        if (!getLabel().equals(option.getLabel())) return false;
        if (!getId().equals(option.getId())) return false;
        return getValue().equals(option.getValue());
    }

    @Override
    public int hashCode() {
        int result = getLabel().hashCode();
        result = 31 * result + getValue().hashCode();
        result = 31 * result + getId().hashCode();
        return result;
    }
}
