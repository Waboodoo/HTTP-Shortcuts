package ch.rmy.android.http_shortcuts.realm.models;

import io.realm.RealmObject;

public class Option extends RealmObject {

    private String label;
    private String value;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Option option = (Option) o;

        if (!getLabel().equals(option.getLabel())) return false;
        return getValue().equals(option.getValue());
    }

    @Override
    public int hashCode() {
        int result = getLabel().hashCode();
        result = 31 * result + getValue().hashCode();
        return result;
    }
}
