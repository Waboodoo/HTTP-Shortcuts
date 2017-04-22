package ch.rmy.android.http_shortcuts.adapters;

import android.content.Context;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.listeners.OnItemClickedListener;
import ch.rmy.android.http_shortcuts.realm.models.HasId;
import ch.rmy.android.http_shortcuts.utils.Destroyable;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmObject;

public abstract class BaseAdapter<T extends RealmObject & HasId> extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Destroyable {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_EMPTY_MARKER = 1;

    private static final int ID_EMPTY_MARKER = -1;

    protected final Context context;
    OnItemClickedListener<T> clickListener;
    private RealmList<T> items;

    private final RealmChangeListener<RealmList<T>> changeListener = new RealmChangeListener<RealmList<T>>() {
        @Override
        public void onChange(RealmList<T> items) {
            notifyDataSetChanged();
        }
    };

    BaseAdapter(Context context) {
        this.context = context;
        setHasStableIds(true);
    }

    public final void setItems(RealmList<T> items) {
        if (this.items != null) {
            this.items.removeChangeListener(changeListener);
        }
        this.items = items;
        if (this.items != null) {
            this.items.addChangeListener(changeListener);
        }
        notifyDataSetChanged();
    }

    @Override
    public final void destroy() {
        if (this.items != null) {
            this.items.removeChangeListener(changeListener);
        }
    }

    public final void setOnItemClickListener(OnItemClickedListener<T> clickListener) {
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

    private T getItem(int position) {
        return items.get(position);
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
        return items == null || !items.isValid() ? 0 : items.size();
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

        EmptyMarkerViewHolder(ViewGroup parent, @StringRes int textRes) {
            super(LayoutInflater.from(context).inflate(R.layout.list_empty_item, parent, false));
            if (textRes != 0) {
                ((TextView) itemView.findViewById(R.id.empty_marker)).setText(textRes);
            }
        }
    }

}
