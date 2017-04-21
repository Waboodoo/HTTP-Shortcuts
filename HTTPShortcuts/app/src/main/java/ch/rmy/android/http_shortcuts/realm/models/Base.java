package ch.rmy.android.http_shortcuts.realm.models;

import io.realm.RealmList;
import io.realm.RealmObject;

public class Base extends RealmObject {

    private long version = 6;
    private RealmList<Category> categories;
    private RealmList<Variable> variables;

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public RealmList<Category> getCategories() {
        return categories;
    }

    public void setCategories(RealmList<Category> categories) {
        this.categories = categories;
    }

    public RealmList<Variable> getVariables() {
        return variables;
    }

    public void setVariables(RealmList<Variable> variables) {
        this.variables = variables;
    }

}
