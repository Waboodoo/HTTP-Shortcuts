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

    private final Context context;
    private final List<Shortcut> shortcuts = new ArrayList<>();
    private OnShortcutClickedListener clickListener;

    public ShortcutAdapter(Context context) {
        this.context = context;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return shortcuts.get(position).getID();
    }

    public void updateShortcuts(List<Shortcut> shortcuts) {
        this.shortcuts.clear();
        this.shortcuts.addAll(shortcuts);
    }

    public void setOnShortcutClickListener(OnShortcutClickedListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public int getItemCount() {
        return shortcuts.size();
    }

    @Override
    public ShortcutViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ShortcutViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ShortcutViewHolder) holder).setShortcut(shortcuts.get(position));
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

}
