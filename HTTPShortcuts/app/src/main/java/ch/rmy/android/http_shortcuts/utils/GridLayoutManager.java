package ch.rmy.android.http_shortcuts.utils;

import android.content.Context;
import android.util.DisplayMetrics;

public class GridLayoutManager extends android.support.v7.widget.GridLayoutManager {

    private boolean empty;

    public GridLayoutManager(Context context) {
        super(context, getNumberOfColumns(context));

        setSpanSizeLookup(new android.support.v7.widget.GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position == 0 && empty) {
                    return getSpanCount();
                } else {
                    return 1;
                }
            }
        });
    }

    private static int getNumberOfColumns(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        return (int) (dpWidth / 90);
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }


}
