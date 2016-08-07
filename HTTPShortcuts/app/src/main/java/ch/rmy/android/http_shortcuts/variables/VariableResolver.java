package ch.rmy.android.http_shortcuts.variables;

import android.content.Context;
import android.os.Handler;

import org.jdeferred.Deferred;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

import java.util.List;

import ch.rmy.android.http_shortcuts.realm.models.Shortcut;
import ch.rmy.android.http_shortcuts.realm.models.Variable;

public class VariableResolver {

    public VariableResolver(Context context) {

    }

    public Promise<ResolvedVariables, Throwable, Void> resolve(Shortcut shortcut, List<Variable> variables) {
        final Deferred<ResolvedVariables, Throwable, Void> deferred = new DeferredObject<>();

        //TODO: Implement UI interactions and remove this test code
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ResolvedVariables resolvedVariables = new ResolvedVariables.Builder()
                        .add("test", "cats")
                        .build();

                deferred.resolve(resolvedVariables);
            }
        }, 5000);


        return deferred.promise();
    }

}
