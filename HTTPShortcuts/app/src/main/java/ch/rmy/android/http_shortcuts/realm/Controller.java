package ch.rmy.android.http_shortcuts.realm;

import android.content.Context;

import java.util.List;

import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.legacy_database.LegacyMigration;
import ch.rmy.android.http_shortcuts.realm.models.Base;
import ch.rmy.android.http_shortcuts.realm.models.Category;
import ch.rmy.android.http_shortcuts.realm.models.PendingExecution;
import ch.rmy.android.http_shortcuts.realm.models.Shortcut;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class Controller {

    private static final String FIELD_ID = "id";

    private final Realm realm;

    public Controller(Context context) {
        realm = RealmFactory.getRealm(context);

        if (realm.where(Base.class).count() == 0) {
            setupBase(context);

            LegacyMigration migration = new LegacyMigration(context, this);
            migration.migrate();
        }
    }

    private void setupBase(Context context) {
        final String defaultCategoryName = context.getString(R.string.shortcuts);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Category defaultCategory = Category.createNew(defaultCategoryName);
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

    private Base getBase() {
        return realm.where(Base.class).findFirst();
    }

    public Base exportBase() {
        return realm.copyFromRealm(getBase());
    }

    public void importBase(final Base base) {
        final Base oldBase = getBase();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                List<Category> persistedCategories = realm.copyToRealmOrUpdate(base.getCategories());
                oldBase.getCategories().clear();
                oldBase.getCategories().addAll(persistedCategories);
            }
        });
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

    public RealmResults<PendingExecution> getShortcutsPendingExecution() {
        return realm
                .where(PendingExecution.class)
                .findAllSorted(PendingExecution.FIELD_ENQUEUED_AT);
    }

    public void createPendingExecution(final Shortcut shortcut) {
        long existingPendingExecutions = realm
                .where(PendingExecution.class)
                .equalTo(PendingExecution.FIELD_SHORTCUT_ID, shortcut.getId())
                .count();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(PendingExecution.createNew(shortcut));
            }
        });
    }

    public void removePendingExecution(final PendingExecution pendingExecution) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                pendingExecution.removeFromRealm();
            }
        });
    }

    public void refresh() {
        realm.refresh();
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
