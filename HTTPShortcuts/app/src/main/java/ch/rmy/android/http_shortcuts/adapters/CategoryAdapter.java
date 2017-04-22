package ch.rmy.android.http_shortcuts.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.icons.IconView;
import ch.rmy.android.http_shortcuts.realm.models.Category;
import ch.rmy.android.http_shortcuts.realm.models.Shortcut;

public class CategoryAdapter extends BaseAdapter<Category> {

    private static final int MAX_ICONS = 5;

    public CategoryAdapter(Context context) {
        super(context);
    }

    @Override
    protected CategoryViewHolder createViewHolder(ViewGroup parentView) {
        return new CategoryViewHolder(parentView);
    }

    public class CategoryViewHolder extends BaseViewHolder<Category> {

        @Bind(R.id.name)
        TextView name;
        @Bind(R.id.description)
        TextView description;
        @Bind(R.id.small_icons)
        ViewGroup smallIconContainer;
        @Bind(R.id.layout_type_icon)
        ImageView layoutTypeIcon;

        public CategoryViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(context).inflate(R.layout.list_item_category, parent, false), CategoryAdapter.this);
            ButterKnife.bind(this, itemView);
        }

        @Override
        protected void updateViews(Category category) {
            name.setText(category.getName());
            int count = category.getShortcuts().size();
            description.setText(context.getResources().getQuantityString(R.plurals.shortcut_count, count, count));

            updateIcons(category.getShortcuts());
            updateLayoutTypeIcon(category.getLayoutType());
        }

        private void updateIcons(List<Shortcut> shortcuts) {
            updateIconNumber(Math.min(shortcuts.size(), MAX_ICONS));
            int i = 0;
            for (Shortcut shortcut : shortcuts) {
                IconView icon = (IconView) smallIconContainer.getChildAt(i);
                icon.setImageURI(shortcut.getIconURI(context), shortcut.getIconName());
                i++;
                if (i >= MAX_ICONS) {
                    break;
                }
            }
        }

        private void updateIconNumber(int number) {
            int size = context.getResources().getDimensionPixelSize(R.dimen.small_icon_size);
            while (smallIconContainer.getChildCount() < number) {
                View icon = new IconView(context);
                icon.setLayoutParams(new LinearLayout.LayoutParams(size, size));
                smallIconContainer.addView(icon);
            }
            while (smallIconContainer.getChildCount() > number) {
                smallIconContainer.removeViewAt(0);
            }
        }

        private void updateLayoutTypeIcon(String layoutType) {
            switch (layoutType) {
                case Category.LAYOUT_GRID: {
                    layoutTypeIcon.setImageResource(R.drawable.ic_grid);
                    break;
                }
                case Category.LAYOUT_LINEAR_LIST:
                default: {
                    layoutTypeIcon.setImageResource(R.drawable.ic_list);
                    break;
                }
            }
        }

    }

}
