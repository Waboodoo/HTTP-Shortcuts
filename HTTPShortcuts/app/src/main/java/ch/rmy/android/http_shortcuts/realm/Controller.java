package ch.rmy.android.http_shortcuts.realm;

import android.content.Context;

import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.realm.models.Base;
import ch.rmy.android.http_shortcuts.realm.models.Category;
import ch.rmy.android.http_shortcuts.realm.models.Shortcut;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class Controller {

    private static final int SCHEMA_VERSION = 1;
    private static final String FIELD_ID = "id";

    private final Realm realm;

    public Controller(Context context) {
        RealmConfiguration config = new RealmConfiguration.Builder(context)
                .schemaVersion(SCHEMA_VERSION)
                .build();

        realm = Realm.getInstance(config);

        if (realm.where(Base.class).count() == 0) {
            setupBase(context);
        }
    }

    private void setupBase(Context context) {
        final String defaultCategoryName = context.getString(R.string.shortcuts);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Category defaultCategory = new Category();
                defaultCategory.setName(defaultCategoryName);
                defaultCategory.setId(generateId(Category.class));

                Base newBase = new Base();
                newBase.setCategories(new RealmList<Category>());
                newBase.getCategories().add(defaultCategory);
                realm.copyToRealm(newBase);
            }
        });
    }

    public void destroy() {
        realm.close();
    }

    public Category getCategoryById(long id) {
        return realm.where(Category.class).equalTo(FIELD_ID, id).findFirst();
    }

    public Shortcut getShortcutById(long id) {
        return realm.where(Shortcut.class).equalTo(FIELD_ID, id).findFirst();
    }

    public Shortcut getDetachedShortcutById(long id) {
        Shortcut shortcut = getShortcutById(id);
        if (shortcut == null) {
            return null;
        }
        return realm.copyFromRealm(shortcut);
    }

    public Base getBase() {
        return realm.where(Base.class).findFirst();
    }

    public RealmList<Category> getCategories() {
        return getBase().getCategories();
    }

    public void deleteShortcut(final Shortcut shortcut) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                shortcut.removeFromRealm();
            }
        });
    }

    public void moveShortcut(final Shortcut shortcut, final int position) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for (Category category : getCategories()) {
                    int oldPosition = category.getShortcuts().indexOf(shortcut);
                    if (oldPosition != -1) {
                        category.getShortcuts().move(oldPosition, position);
                    } else {
                        category.getShortcuts().remove(shortcut);
                    }
                }
            }
        });
    }

    public void moveShortcut(final Shortcut shortcut, final Category targetCategory) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for (Category category : getCategories()) {
                    category.getShortcuts().remove(shortcut);
                }
                targetCategory.getShortcuts().add(shortcut);
            }
        });
    }

    public RealmResults<Shortcut> getShortcutsPendingExecution() {
        return realm.where(Shortcut.class).equalTo(Shortcut.FIELD_RETRY_STATUS, Shortcut.RETRY_STATUS_WAITING).findAll();
    }

    public Shortcut persist(final Shortcut shortcut) {
        if (shortcut.isNew()) {
            shortcut.setId(generateId(Shortcut.class));
        }

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(shortcut);
            }
        });
        return getShortcutById(shortcut.getId());
    }

    private long generateId(Class<? extends RealmObject> clazz) {
        Number maxId = realm.where(clazz).max(FIELD_ID);
        long maxIdLong = maxId != null ? maxId.longValue() : 0;
        return maxIdLong + 1;
    }

}
