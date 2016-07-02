package ch.rmy.android.http_shortcuts;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import ch.rmy.android.http_shortcuts.listeners.OnItemClickedListener;
import ch.rmy.android.http_shortcuts.realm.models.HasId;
import ch.rmy.android.http_shortcuts.utils.Destroyable;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;

public abstract class BaseAdapter<T extends RealmObject, U extends RealmObject & HasId> extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Destroyable {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_EMPTY_MARKER = 1;

    private static final int ID_EMPTY_MARKER = -1;

    protected final Context context;
    private OnItemClickedListener<U> clickListener;
    private T parent;

    private final RealmChangeListener<T> changeListener = new RealmChangeListener<T>() {
        @Override
        public void onChange(T parent) {
            notifyDataSetChanged();
        }
    };

    public BaseAdapter(Context context) {
        this.context = context;
        setHasStableIds(true);
    }

    public final void setParent(T parent) {
        if (this.parent != null) {
            this.parent.removeChangeListener(changeListener);
        }
        this.parent = parent;
        if (this.parent != null) {
            this.parent.addChangeListener(changeListener);
        }
        notifyDataSetChanged();
    }

    @Override
    public final void destroy() {
        if (this.parent != null) {
            this.parent.removeChangeListener(changeListener);
        }
    }

    public final void setOnItemClickListener(OnItemClickedListener<U> clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public final int getItemViewType(int position) {
        if (isEmpty()) {
            return TYPE_EMPTY_MARKER;
        } else {
            return TYPE_ITEM;
        }
    }

    protected abstract List<U> getItems(T parent);

    protected final U getItem(int position) {
        return getItems(parent).get(position);
    }

    @Override
    public final long getItemId(int position) {
        return isEmpty() ? ID_EMPTY_MARKER : getItem(position).getId();
    }

    @Override
    public final int getItemCount() {
        return isEmpty() ? 1 : getCount();
    }

    protected final int getCount() {
        return parent == null ? 0 : getItems(parent).size();
    }

    protected final boolean isEmpty() {
        return parent == null || getItems(parent).isEmpty();
    }

    @Override
    public final RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return viewType == TYPE_EMPTY_MARKER ? new EmptyMarkerViewHolder(parent) : createViewHolder(parent);
    }

    protected abstract BaseViewHolder createViewHolder(ViewGroup parentView);

    @Override
    public final void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((BaseViewHolder) holder).setItem(getItem(position));
    }

    protected abstract class BaseViewHolder extends RecyclerView.ViewHolder {

        private U item;

        public BaseViewHolder(View parent) {
            super(parent);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickListener != null) {
                        clickListener.onItemClicked(item);
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (clickListener != null) {
                        clickListener.onItemLongClicked(item);
                        return true;
                    }
                    return false;
                }
            });
        }

        public void setItem(U item) {
            this.item = item;
            updateViews(item);
        }

        protected abstract void updateViews(U item);

    }

    private class EmptyMarkerViewHolder extends RecyclerView.ViewHolder {

        public EmptyMarkerViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(context).inflate(R.layout.list_empty_item, parent, false));
        }
    }

}
