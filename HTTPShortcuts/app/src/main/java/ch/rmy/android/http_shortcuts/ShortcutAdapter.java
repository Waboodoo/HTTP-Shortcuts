package ch.rmy.android.http_shortcuts;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import ch.rmy.android.http_shortcuts.icons.IconView;
import ch.rmy.android.http_shortcuts.listeners.OnShortcutClickedListener;
import ch.rmy.android.http_shortcuts.realm.models.Category;
import ch.rmy.android.http_shortcuts.realm.models.Shortcut;
import io.realm.RealmChangeListener;

public class ShortcutAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SHORTCUT = 0;
    private static final int TYPE_EMPTY_MARKER = 1;

    private final Context context;
    private final Category category;
    private OnShortcutClickedListener clickListener;

    public ShortcutAdapter(Context context, Category category) {
        this.context = context;
        this.category = category;
        setHasStableIds(true);

        category.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        if (category.getShortcuts().isEmpty()) {
            return TYPE_EMPTY_MARKER;
        } else {
            return TYPE_SHORTCUT;
        }
    }

    @Override
    public long getItemId(int position) {
        if (category.getShortcuts().isEmpty()) {
            return -1;
        }
        return category.getShortcuts().get(position).getId();
    }

    public void setOnShortcutClickListener(OnShortcutClickedListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public int getItemCount() {
        if (category.getShortcuts().isEmpty()) {
            return 1;
        } else {
            return category.getShortcuts().size();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_EMPTY_MARKER) {
            return new EmptyMarkerViewHolder(parent);
        } else {
            return new ShortcutViewHolder(parent);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ShortcutViewHolder) {
            ((ShortcutViewHolder) holder).setShortcut(category.getShortcuts().get(position));
        }
    }

    public class ShortcutViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.name)
        TextView name;
        @Bind(R.id.description)
        TextView description;
        @Bind(R.id.icon)
        IconView icon;
        private Shortcut shortcut;

        public ShortcutViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(context).inflate(R.layout.list_item, parent, false));
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickListener != null) {
                        clickListener.onShortcutClicked(shortcut, v);
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (clickListener != null) {
                        clickListener.onShortcutLongClicked(shortcut, itemView);
                        return true;
                    }
                    return false;
                }
            });
        }

        public void setShortcut(Shortcut shortcut) {
            this.shortcut = shortcut;
            name.setText(shortcut.getName());
            description.setText(shortcut.getDescription());
            description.setVisibility(TextUtils.isEmpty(shortcut.getDescription()) ? View.GONE : View.VISIBLE);
            icon.setImageURI(shortcut.getIconURI(context), shortcut.getIconName());
        }

    }

    private class EmptyMarkerViewHolder extends RecyclerView.ViewHolder {

        public EmptyMarkerViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(context).inflate(R.layout.list_empty_item, parent, false));
        }
    }

}
