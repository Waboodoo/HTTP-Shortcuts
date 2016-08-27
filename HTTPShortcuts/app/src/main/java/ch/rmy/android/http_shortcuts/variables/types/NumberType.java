package ch.rmy.android.http_shortcuts.variables.types;

import android.text.InputType;

import com.afollestad.materialdialogs.MaterialDialog;

import org.jdeferred.Deferred;

import ch.rmy.android.http_shortcuts.realm.Controller;
import ch.rmy.android.http_shortcuts.realm.models.Variable;

public class NumberType extends TextType {

    @Override
    public void setupDialog(Controller controller, Variable variable, MaterialDialog.Builder builder, Deferred<String, Void, Void> deferredValue) {
        super.setupDialog(controller, variable, builder, deferredValue);
        builder.inputType(InputType.TYPE_CLASS_NUMBER);
    }

}
