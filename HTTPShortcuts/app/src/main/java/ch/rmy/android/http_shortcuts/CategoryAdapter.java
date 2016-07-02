package ch.rmy.android.http_shortcuts;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import ch.rmy.android.http_shortcuts.listeners.OnItemClickedListener;
import ch.rmy.android.http_shortcuts.realm.models.Base;
import ch.rmy.android.http_shortcuts.realm.models.Category;
import ch.rmy.android.http_shortcuts.utils.Destroyable;
import io.realm.RealmChangeListener;

public class CategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Destroyable {

    private final Context context;
    private OnItemClickedListener<Category> clickListener;
    private final Base base;

    private final RealmChangeListener<Base> changeListener = new RealmChangeListener<Base>() {
        @Override
        public void onChange(Base base) {
            notifyDataSetChanged();
        }
    };

    public CategoryAdapter(Context context, Base base) {
        this.context = context;
        this.base = base;
        setHasStableIds(true);

        this.base.addChangeListener(changeListener);
    }

    @Override
    public void destroy() {
        this.base.removeChangeListener(changeListener);
    }

    public void setOnCategoryClickListener(OnItemClickedListener<Category> clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public long getItemId(int position) {
        return base.getCategories().get(position).getId();
    }

    @Override
    public int getItemCount() {
        return base.getCategories().size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CategoryViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((CategoryViewHolder) holder).setCategory(base.getCategories().get(position));
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.name)
        TextView name;
        private Category category;

        public CategoryViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(context).inflate(R.layout.category_list_item, parent, false));
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickListener != null) {
                        clickListener.onItemClicked(category);
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (clickListener != null) {
                        clickListener.onItemLongClicked(category);
                        return true;
                    }
                    return false;
                }
            });
        }

        public void setCategory(Category category) {
            this.category = category;
            name.setText(category.getName());
        }

    }

}
