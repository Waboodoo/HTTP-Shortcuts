package ch.rmy.android.http_shortcuts.adapters;

import android.content.Context;

import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.realm.models.PendingExecution;
import ch.rmy.android.http_shortcuts.realm.models.Shortcut;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public abstract class ShortcutAdapter extends BaseAdapter<Shortcut> {

    private final RealmChangeListener<RealmResults<PendingExecution>> changeListener = new RealmChangeListener<RealmResults<PendingExecution>>() {
        @Override
        public void onChange(RealmResults<PendingExecution> results) {
            notifyDataSetChanged();
        }
    };
    RealmResults<PendingExecution> shortcutsPendingExecution;

    ShortcutAdapter(Context context) {
        super(context);
    }

    @Override
    protected int getEmptyMarkerStringResource() {
        return R.string.no_shortcuts;
    }

    public void setPendingShortcuts(RealmResults<PendingExecution> shortcutsPendingExecution) {
        if (this.shortcutsPendingExecution != null) {
            this.shortcutsPendingExecution.removeChangeListener(changeListener);
        }
        this.shortcutsPendingExecution = shortcutsPendingExecution;
        if (this.shortcutsPendingExecution != null) {
            this.shortcutsPendingExecution.addChangeListener(changeListener);
        }
    }

}
