package ch.rmy.android.http_shortcuts.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.icons.IconView;
import ch.rmy.android.http_shortcuts.realm.models.Category;
import ch.rmy.android.http_shortcuts.realm.models.PendingExecution;
import ch.rmy.android.http_shortcuts.realm.models.Shortcut;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class ShortcutAdapter extends BaseAdapter<Category, Shortcut> {

    private final RealmChangeListener<RealmResults<PendingExecution>> changeListener = new RealmChangeListener<RealmResults<PendingExecution>>() {
        @Override
        public void onChange(RealmResults<PendingExecution> results) {
            notifyDataSetChanged();
        }
    };
    private RealmResults<PendingExecution> shortcutsPendingExecution;

    public ShortcutAdapter(Context context) {
        super(context);
    }

    @Override
    protected ShortcutViewHolder createViewHolder(ViewGroup parentView) {
        return new ShortcutViewHolder(parentView);
    }

    @Override
    protected List<Shortcut> getItems(Category category) {
        return category.getShortcuts();
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

    public class ShortcutViewHolder extends BaseViewHolder<Shortcut> {

        @Bind(R.id.name)
        TextView name;
        @Bind(R.id.description)
        TextView description;
        @Bind(R.id.icon)
        IconView icon;
        @Bind(R.id.waiting_icon)
        View waitingIcon;

        public ShortcutViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(context).inflate(R.layout.shortcut_list_item, parent, false), ShortcutAdapter.this);
            ButterKnife.bind(this, itemView);
        }

        @Override
        protected void updateViews(Shortcut shortcut) {
            name.setText(shortcut.getName());
            description.setText(shortcut.getDescription());
            description.setVisibility(TextUtils.isEmpty(shortcut.getDescription()) ? View.GONE : View.VISIBLE);
            icon.setImageURI(shortcut.getIconURI(context), shortcut.getIconName());
            waitingIcon.setVisibility(isPendingExecution(shortcut.getId()) ? View.VISIBLE : View.GONE);
        }

        private boolean isPendingExecution(long shortcutId) {
            for (PendingExecution pendingExecution : shortcutsPendingExecution) {
                if (pendingExecution.getShortcutId() == shortcutId) {
                    return true;
                }
            }
            return false;
        }

    }


}
