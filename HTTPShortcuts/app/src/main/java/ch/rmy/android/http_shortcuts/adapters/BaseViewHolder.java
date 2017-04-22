package ch.rmy.android.http_shortcuts.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import ch.rmy.android.http_shortcuts.realm.models.HasId;
import io.realm.RealmObject;

abstract class BaseViewHolder<T extends RealmObject & HasId> extends RecyclerView.ViewHolder {

    private T item;

    BaseViewHolder(View parent, final BaseAdapter<T> baseAdapter) {
        super(parent);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (baseAdapter.clickListener != null) {
                    baseAdapter.clickListener.onItemClicked(item);
                }
            }
        });
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (baseAdapter.clickListener != null) {
                    baseAdapter.clickListener.onItemLongClicked(item);
                    return true;
                }
                return false;
            }
        });
    }

    public void setItem(T item) {
        this.item = item;
        updateViews(item);
    }

    protected abstract void updateViews(T item);

}