package ch.rmy.android.http_shortcuts.realm.models;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Category extends RealmObject implements HasId {

    public static final String LAYOUT_LINEAR_LIST = "linear_list";
    public static final String LAYOUT_GRID = "grid";

    @PrimaryKey
    private long id;
    @Required
    private String name;
    private RealmList<Shortcut> shortcuts;
    private String layoutType;

    @Override
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

    public String getLayoutType() {
        return layoutType;
    }

    public void setLayoutType(String layoutType) {
        this.layoutType = layoutType;
    }

    @Override
    public boolean isNew() {
        return id == 0;
    }

    public static Category createNew(String name) {
        Category category = new Category();
        category.setId(0);
        category.setName(name);
        category.setShortcuts(new RealmList<Shortcut>());
        category.setLayoutType(LAYOUT_LINEAR_LIST);
        return category;
    }


    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Category category = (Category) o;

        if (getId() != category.getId()) return false;
        if (!getName().equals(category.getName())) return false;
        if (!getLayoutType().equals(category.getLayoutType())) return false;
        return getShortcuts().equals(category.getShortcuts());
    }

    @Override
    public int hashCode() {
        int result = (int) (getId() ^ (getId() >>> 32));
        result = 31 * result + getName().hashCode();
        result = 31 * result + getLayoutType().hashCode();
        result = 31 * result + getShortcuts().hashCode();
        return result;
    }

}
