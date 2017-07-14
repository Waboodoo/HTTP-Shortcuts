package ch.rmy.android.http_shortcuts.icons;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.utils.UIUtil;

public class IconView extends AppCompatImageView {

    private String iconName;

    public IconView(Context context) {
        super(context);
    }

    public IconView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IconView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        iconName = getContext().getResources().getResourceEntryName(resId);
        updateBackground();
    }

    private void updateBackground() {
        if (requiresBackground()) {
            setBackgroundResource(R.drawable.icon_background);
        } else {
            UIUtil.clearBackground(this);
        }
    }

    private boolean requiresBackground() {
        return iconName != null && iconName.startsWith("white_");
    }

    public String getIconName() {
        return iconName;
    }

    public void setImageURI(Uri uri, String iconName) {
        setImageURI(uri);
        this.iconName = iconName;
        updateBackground();
    }

}
