package ch.rmy.android.http_shortcuts.realm;

import android.content.Context;

import ch.rmy.android.http_shortcuts.realm.models.Shortcut;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class Controller {

    private static final int SCHEMA_VERSION = 1;

    public static final String DIRECTION_UP = "UP";
    public static final String DIRECTION_DOWN = "DOWN";

    private final Realm realm;

    public Controller(Context context) {
        RealmConfiguration config = new RealmConfiguration.Builder(context)
                .schemaVersion(SCHEMA_VERSION)
                .build();

        realm = Realm.getInstance(config);
    }

    public void destroy() {
        realm.close();
    }

    public Shortcut getShortcutById(long id) {
        return realm.where(Shortcut.class).equalTo(Shortcut.FIELD_ID, id).findFirst();
    }

    public Shortcut getDetachedShortcutById(long id) {
        Shortcut shortcut = getShortcutById(id);
        if (shortcut == null) {
            return null;
        }
        return realm.copyFromRealm(shortcut);
    }

    public RealmResults<Shortcut> getShortcuts() {
        return realm.where(Shortcut.class).findAllSorted(Shortcut.FIELD_POSITION);
    }

    public void deleteShortcut(final Shortcut shortcut) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                shortcut.removeFromRealm();
                // TODO: Update positions?
            }
        });
    }

    public void duplicateShortcut(final Shortcut shortcut, String newName) {
        Shortcut duplicate = shortcut.duplicate(newName);
        copyToRealm(duplicate);
    }

    public void moveShortcut(final Shortcut shortcut, final String direction) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                shortcut.setPosition(shortcut.getPosition() + (DIRECTION_UP.equals(direction) ? -1 : 1));
            }
        });
    }

    public RealmResults<Shortcut> getShortcutsPendingExecution() {
        return realm.where(Shortcut.class).equalTo(Shortcut.FIELD_RETRY_STATUS, Shortcut.RETRY_STATUS_WAITING).findAll();
    }

    public long copyToRealm(final Shortcut shortcut) {
        if (shortcut.isNew()) {
            shortcut.setId(generateId());
        }

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(shortcut);
            }
        });
        return shortcut.getId();
    }

    private long generateId() {
        return realm.where(Shortcut.class).max(Shortcut.FIELD_ID).longValue() + 1;
    }

}
