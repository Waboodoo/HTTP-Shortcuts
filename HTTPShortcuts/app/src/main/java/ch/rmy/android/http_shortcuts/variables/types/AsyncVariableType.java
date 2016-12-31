package ch.rmy.android.http_shortcuts.variables.types;

import android.content.Context;

import org.jdeferred.Deferred;

import ch.rmy.android.http_shortcuts.realm.Controller;
import ch.rmy.android.http_shortcuts.realm.models.Variable;
import ch.rmy.android.http_shortcuts.variables.Showable;

public interface AsyncVariableType {

    Showable createDialog(Context context, Controller controller, Variable variable, Deferred<String, Void, Void> deferredValue);

    boolean hasTitle();

}
