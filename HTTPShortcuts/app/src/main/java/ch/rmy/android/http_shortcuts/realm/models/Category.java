package ch.rmy.android.http_shortcuts.realm.models;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Category extends RealmObject {

    @PrimaryKey
    private long id;
    @Required
    private String name;
    private RealmList<Shortcut> shortcuts;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public RealmList<Shortcut> getShortcuts() {
        return shortcuts;
    }

    public void setShortcuts(RealmList<Shortcut> shortcuts) {
        this.shortcuts = shortcuts;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
