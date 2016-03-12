package ch.rmy.android.http_shortcuts.shortcuts;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.listeners.OnShortcutClickedListener;

public class ShortcutAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SHORTCUT = 0;
    private static final int TYPE_EMPTY_MARKER = 1;

    private final Context context;
    private final List<Shortcut> shortcuts = new ArrayList<>();
    private OnShortcutClickedListener clickListener;

    public ShortcutAdapter(Context context) {
        this.context = context;
        setHasStableIds(true);
    }

    @Override
    public int getItemViewType(int position) {
        if (shortcuts.isEmpty()) {
            return TYPE_EMPTY_MARKER;
        } else {
            return TYPE_SHORTCUT;
        }
    }

    @Override
    public long getItemId(int position) {
        if (shortcuts.isEmpty()) {
            return -1;
        }
        return shortcuts.get(position).getID();
    }

    public void updateShortcuts(List<Shortcut> shortcuts) {
        this.shortcuts.clear();
        this.shortcuts.addAll(shortcuts);
        notifyDataSetChanged();
    }

    public void setOnShortcutClickListener(OnShortcutClickedListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public int getItemCount() {
        if (shortcuts.isEmpty()) {
            return 1;
        } else {
            return shortcuts.size();
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
            ((ShortcutViewHolder) holder).setShortcut(shortcuts.get(position));
        }
    }

    public class ShortcutViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.name)
        TextView name;
        @Bind(R.id.description)
        TextView description;
        @Bind(R.id.icon)
        ImageView icon;
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
            icon.setImageURI(shortcut.getIconURI(context));
            icon.setBackgroundColor(getAppropriateBackgroundColor(shortcut.getIconName()));
        }

        private int getAppropriateBackgroundColor(String iconName) {
            if (iconName != null && iconName.startsWith("white_")) {
                return Color.BLACK;
            } else {
                return Color.TRANSPARENT;
            }
        }

    }

    private class EmptyMarkerViewHolder extends RecyclerView.ViewHolder {

        public EmptyMarkerViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(context).inflate(R.layout.list_empty_item, parent, false));
        }
    }

}
