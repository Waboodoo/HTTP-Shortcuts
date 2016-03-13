package com.afollestad.materialdialogs.prefs;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.preference.Preference;
import android.util.AttributeSet;

import com.afollestad.materialdialogs.prefs.PrefUtil;

public class MaterialPreference extends Preference {

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MaterialPreference(Context context) {
        super(context);
        init(context, null);
    }

    public MaterialPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MaterialPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MaterialPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }


    private void init(Context context, AttributeSet attrs) {
        PrefUtil.setLayoutResource(context, this, attrs);
    }

}
