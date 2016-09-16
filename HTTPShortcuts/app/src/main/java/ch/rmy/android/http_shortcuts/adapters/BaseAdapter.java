package ch.rmy.android.http_shortcuts.adapters;

import android.content.Context;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ch.rmy.android.http_shortcuts.R;
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
    protected OnItemClickedListener<U> clickListener;
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
        return parent == null || !parent.isValid() ? 0 : getItems(parent).size();
    }

    protected final boolean isEmpty() {
        return getCount() == 0;
    }

    @Override
    public final RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return viewType == TYPE_EMPTY_MARKER ? new EmptyMarkerViewHolder(parent, getEmptyMarkerStringResource()) : createViewHolder(parent);
    }

    protected abstract BaseViewHolder createViewHolder(ViewGroup parentView);

    protected int getEmptyMarkerStringResource() {
        return 0;
    }

    @Override
    public final void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof BaseViewHolder) {
            ((BaseViewHolder) holder).setItem(getItem(position));
        }
    }

    private class EmptyMarkerViewHolder extends RecyclerView.ViewHolder {

        public EmptyMarkerViewHolder(ViewGroup parent, @StringRes int textRes) {
            super(LayoutInflater.from(context).inflate(R.layout.list_empty_item, parent, false));
            if (textRes != 0) {
                ((TextView) itemView.findViewById(R.id.empty_marker)).setText(textRes);
            }
        }
    }

}
