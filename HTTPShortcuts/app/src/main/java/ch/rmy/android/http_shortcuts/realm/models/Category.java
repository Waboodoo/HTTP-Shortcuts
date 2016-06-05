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

    public static Category createNew(String name) {
        Category category = new Category();
        category.setId(0);
        category.setName(name);
        category.setShortcuts(new RealmList<Shortcut>());
        return category;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Category category = (Category) o;

        if (getId() != category.getId()) return false;
        if (!getName().equals(category.getName())) return false;
        return getShortcuts().equals(category.getShortcuts());
    }

    @Override
    public int hashCode() {
        int result = (int) (getId() ^ (getId() >>> 32));
        result = 31 * result + getName().hashCode();
        result = 31 * result + getShortcuts().hashCode();
        return result;
    }

}
