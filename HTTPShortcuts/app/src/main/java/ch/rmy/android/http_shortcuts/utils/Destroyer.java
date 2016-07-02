package ch.rmy.android.http_shortcuts.utils;

import java.util.HashSet;
import java.util.Set;

public class Destroyer implements Destroyable {

    private final Set<Destroyable> destroyables = new HashSet<>();

    public <T extends Destroyable> T own(T destroyable) {
        destroyables.add(destroyable);
        return destroyable;
    }

    @Override
    public void destroy() {
        for (Destroyable destroyable : destroyables) {
            destroyable.destroy();
        }
        destroyables.clear();
    }

}
