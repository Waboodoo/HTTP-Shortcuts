package ch.rmy.android.http_shortcuts.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.realm.models.Base;
import ch.rmy.android.http_shortcuts.realm.models.Category;

public class CategoryAdapter extends BaseAdapter<Base, Category> {

    public CategoryAdapter(Context context) {
        super(context);
    }

    @Override
    protected List<Category> getItems(Base base) {
        return base.getCategories();
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

        public CategoryViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(context).inflate(R.layout.category_list_item, parent, false), CategoryAdapter.this);
            ButterKnife.bind(this, itemView);
        }

        @Override
        protected void updateViews(Category category) {
            name.setText(category.getName());
            int count = category.getShortcuts().size();
            description.setText(context.getResources().getQuantityString(R.plurals.shortcut_count, count, count));
        }

    }

}
