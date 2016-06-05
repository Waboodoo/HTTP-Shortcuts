package ch.rmy.android.http_shortcuts.realm.models;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class PendingExecution extends RealmObject {

    public static final String FIELD_SHORTCUT_ID = "shortcutId";
    public static final String FIELD_ENQUEUED_AT = "enqueuedAt";

    @PrimaryKey
    private long shortcutId;
    @Index
    private Date enqueuedAt;

    public static PendingExecution createNew(Shortcut shortcut) {
        PendingExecution pendingExecution = new PendingExecution();
        pendingExecution.setShortcutId(shortcut.getId());
        pendingExecution.setEnqueuedAt(new Date());
        return pendingExecution;
    }

    public long getShortcutId() {
        return shortcutId;
    }

    public void setShortcutId(long shortcutId) {
        this.shortcutId = shortcutId;
    }

    public Date getEnqueuedAt() {
        return enqueuedAt;
    }

    public void setEnqueuedAt(Date enqueuedAt) {
        this.enqueuedAt = enqueuedAt;
    }

}
