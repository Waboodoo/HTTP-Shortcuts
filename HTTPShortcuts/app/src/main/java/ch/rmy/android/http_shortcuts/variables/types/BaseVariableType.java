package ch.rmy.android.http_shortcuts.variables.types;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;

import com.afollestad.materialdialogs.MaterialDialog;

import org.jdeferred.Deferred;

import ch.rmy.android.http_shortcuts.realm.models.Variable;

public abstract class BaseVariableType {

    public final String getTag() {
        return getClass().getSimpleName();
    }

    static MaterialDialog.Builder createDialogBuilder(Context context, Variable variable, final Deferred<String, Void, Void> deferred) {
        MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(context);
        if (!TextUtils.isEmpty(variable.getTitle())) {
            dialogBuilder.title(variable.getTitle());
        }
        dialogBuilder.dismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (deferred.isPending()) {
                    deferred.reject(null);
                }
            }
        });
        return dialogBuilder;
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
