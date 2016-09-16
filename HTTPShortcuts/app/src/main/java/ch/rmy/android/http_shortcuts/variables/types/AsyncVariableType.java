package ch.rmy.android.http_shortcuts.variables.types;

import com.afollestad.materialdialogs.MaterialDialog;

import org.jdeferred.Deferred;

import ch.rmy.android.http_shortcuts.realm.Controller;
import ch.rmy.android.http_shortcuts.realm.models.Variable;

public interface AsyncVariableType {

    void setupDialog(Controller controller, Variable variable, MaterialDialog.Builder builder, Deferred<String, Void, Void> deferredValue);

}
