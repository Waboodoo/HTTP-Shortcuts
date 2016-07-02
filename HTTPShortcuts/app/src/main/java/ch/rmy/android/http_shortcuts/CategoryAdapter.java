package ch.rmy.android.http_shortcuts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
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

    public class CategoryViewHolder extends BaseViewHolder {

        @Bind(R.id.name)
        TextView name;

        public CategoryViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(context).inflate(R.layout.category_list_item, parent, false));
            ButterKnife.bind(this, itemView);
        }

        @Override
        protected void updateViews(Category category) {
            name.setText(category.getName());
        }

    }

}
