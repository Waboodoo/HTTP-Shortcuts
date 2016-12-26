package ch.rmy.android.http_shortcuts.realm.models;

import java.util.Date;
import java.util.List;

import io.realm.RealmList;
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

    private RealmList<ResolvedVariable> resolvedVariables;

    public static PendingExecution createNew(long shortcutId, List<ResolvedVariable> resolvedVariables) {
        PendingExecution pendingExecution = new PendingExecution();

        RealmList<ResolvedVariable> resolvedVariableList = new RealmList<>();
        resolvedVariableList.addAll(resolvedVariables);

        pendingExecution.setResolvedVariables(resolvedVariableList);
        pendingExecution.setShortcutId(shortcutId);
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

    public RealmList<ResolvedVariable> getResolvedVariables() {
        return resolvedVariables;
    }

    public void setResolvedVariables(RealmList<ResolvedVariable> resolvedVariables) {
        this.resolvedVariables = resolvedVariables;
    }

}
