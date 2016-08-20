package ch.rmy.android.http_shortcuts.variables.types;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

public abstract class BaseVariableType {

    public final String getTag() {
        return getClass().getSimpleName();
    }

    public final VariableEditorFragment getEditorFragment(FragmentManager fragmentManager) {
        Fragment fragment = fragmentManager.findFragmentByTag(getTag());
        if (fragment != null) {
            return (VariableEditorFragment) fragment;
        }
        return createEditorFragment();
    }

    protected VariableEditorFragment createEditorFragment() {
        return new VariableEditorFragment();
    }

}
