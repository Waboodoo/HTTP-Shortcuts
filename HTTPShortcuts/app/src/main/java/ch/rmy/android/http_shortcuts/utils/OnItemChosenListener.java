package ch.rmy.android.http_shortcuts.utils;

import android.view.View;
import android.widget.AdapterView;

import com.farbod.labelledspinner.LabelledSpinner;

public abstract class OnItemChosenListener implements LabelledSpinner.OnItemChosenListener {

    @Override
    public void onItemChosen(View labelledSpinner, AdapterView<?> adapterView, View itemView, int position, long id) {
        onSelectionChanged();
    }

    public abstract void onSelectionChanged();

    @Override
    public void onNothingChosen(View labelledSpinner, AdapterView<?> adapterView) {

    }

}
