package ch.rmy.android.http_shortcuts.realm.models;

import io.realm.RealmList;
import io.realm.RealmObject;

public class Base extends RealmObject {

    private RealmList<Category> categories;

    public RealmList<Category> getCategories() {
        return categories;
    }

    public void setCategories(RealmList<Category> categories) {
        this.categories = categories;
    }

}
