package ch.rmy.android.http_shortcuts.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.icons.IconView;
import ch.rmy.android.http_shortcuts.realm.models.Shortcut;

public class ShortcutGridAdapter extends ShortcutAdapter {

    public ShortcutGridAdapter(Context context) {
        super(context);
    }

    @Override
    protected ShortcutGridAdapter.ShortcutViewHolder createViewHolder(ViewGroup parentView) {
        return new ShortcutGridAdapter.ShortcutViewHolder(parentView);
    }

    public class ShortcutViewHolder extends BaseViewHolder<Shortcut> {

        @Bind(R.id.name)
        TextView name;
        @Bind(R.id.icon)
        IconView icon;

        public ShortcutViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(context).inflate(R.layout.shortcut_grid_item, parent, false), ShortcutGridAdapter.this);
            ButterKnife.bind(this, itemView);
        }

        @Override
        protected void updateViews(Shortcut shortcut) {
            name.setText(shortcut.getName());
            icon.setImageURI(shortcut.getIconURI(context), shortcut.getIconName());
        }

    }

}
