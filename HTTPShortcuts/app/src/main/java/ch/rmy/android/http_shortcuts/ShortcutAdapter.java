package ch.rmy.android.http_shortcuts;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import ch.rmy.android.http_shortcuts.icons.IconView;
import ch.rmy.android.http_shortcuts.realm.models.Category;
import ch.rmy.android.http_shortcuts.realm.models.Shortcut;

public class ShortcutAdapter extends BaseAdapter<Category, Shortcut> {

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

    public class ShortcutViewHolder extends BaseViewHolder {

        @Bind(R.id.name)
        TextView name;
        @Bind(R.id.description)
        TextView description;
        @Bind(R.id.icon)
        IconView icon;

        public ShortcutViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(context).inflate(R.layout.shortcut_list_item, parent, false));
            ButterKnife.bind(this, itemView);
        }

        @Override
        protected void updateViews(Shortcut shortcut) {
            name.setText(shortcut.getName());
            description.setText(shortcut.getDescription());
            description.setVisibility(TextUtils.isEmpty(shortcut.getDescription()) ? View.GONE : View.VISIBLE);
            icon.setImageURI(shortcut.getIconURI(context), shortcut.getIconName());
        }

    }


}
