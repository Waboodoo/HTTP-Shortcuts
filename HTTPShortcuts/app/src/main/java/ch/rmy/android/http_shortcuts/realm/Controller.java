package ch.rmy.android.http_shortcuts.realm;

import android.content.Context;

import java.util.Collection;
import java.util.List;

import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.realm.models.Base;
import ch.rmy.android.http_shortcuts.realm.models.Category;
import ch.rmy.android.http_shortcuts.realm.models.PendingExecution;
import ch.rmy.android.http_shortcuts.realm.models.ResolvedVariable;
import ch.rmy.android.http_shortcuts.realm.models.Shortcut;
import ch.rmy.android.http_shortcuts.realm.models.Variable;
import ch.rmy.android.http_shortcuts.utils.Destroyable;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class Controller implements Destroyable {

    private static final String FIELD_ID = "id";

    private final Realm realm;

    public static void init(Context context) {
        Realm.init(context);

        Realm realm = RealmFactory.getRealm();
        if (realm.where(Base.class).count() == 0) {
            setupBase(context, realm);
        }
        realm.close();
    }

    private static void setupBase(Context context, Realm realm) {
        final String defaultCategoryName = context.getString(R.string.shortcuts);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Category defaultCategory = Category.createNew(defaultCategoryName);
                defaultCategory.setId(1);

                Base newBase = new Base();
                newBase.setCategories(new RealmList<Category>());
                newBase.setVariables(new RealmList<Variable>());
                newBase.getCategories().add(defaultCategory);
                realm.copyToRealm(newBase);
            }
        });
    }

    public Controller() {
        realm = RealmFactory.getRealm();
    }

    @Override
    public void destroy() {
        realm.close();
    }

    public Category getCategoryById(long id) {
        return realm.where(Category.class).equalTo(FIELD_ID, id).findFirst();
    }

    public Shortcut getShortcutById(long id) {
        return realm.where(Shortcut.class).equalTo(FIELD_ID, id).findFirst();
    }


    public Shortcut getShortcutByName(String shortcutName) {
        return realm.where(Shortcut.class).equalTo(Shortcut.FIELD_NAME, shortcutName, Case.INSENSITIVE).findFirst();
    }

    public Variable getVariableById(long id) {
        return realm.where(Variable.class).equalTo(FIELD_ID, id).findFirst();
    }

    public Variable getVariableByKey(String key) {
        return realm.where(Variable.class).equalTo(Variable.FIELD_KEY, key).findFirst();
    }

    public void setVariableValue(final Variable variable, final String value) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                variable.setValue(value);
            }
        });
    }

    public Shortcut getDetachedShortcutById(long id) {
        Shortcut shortcut = getShortcutById(id);
        if (shortcut == null) {
            return null;
        }
        return realm.copyFromRealm(shortcut);
    }

    public Variable getDetachedVariableById(long id) {
        Variable variable = getVariableById(id);
        if (variable == null) {
            return null;
        }
        return realm.copyFromRealm(variable);
    }

    public Base getBase() {
        return realm.where(Base.class).findFirst();
    }

    public Base exportBase() {
        return realm.copyFromRealm(getBase());
    }

    public void importBase(final Base base) {
        final Base oldBase = getBase();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                List<Category> persistedCategories = realm.copyToRealmOrUpdate(base.getCategories());
                oldBase.getCategories().removeAll(persistedCategories);
                oldBase.getCategories().addAll(persistedCategories);

                List<Variable> persistedVariables = realm.copyToRealmOrUpdate(base.getVariables());
                oldBase.getVariables().removeAll(persistedVariables);
                oldBase.getVariables().addAll(persistedVariables);
            }
        });
    }

    public RealmList<Category> getCategories() {
        return getBase().getCategories();
    }

    public RealmList<Variable> getVariables() {
        return getBase().getVariables();
    }

    public void deleteShortcut(final Shortcut shortcut) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                shortcut.getHeaders().deleteAllFromRealm();
                shortcut.getParameters().deleteAllFromRealm();
                shortcut.deleteFromRealm();
            }
        });
    }

    public void moveShortcut(final Shortcut shortcut, final int position) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for (Category category : getCategories()) {
                    int oldPosition = category.getShortcuts().indexOf(shortcut);
                    if (oldPosition != -1) {
                        category.getShortcuts().move(oldPosition, position);
                    } else {
                        category.getShortcuts().remove(shortcut);
                    }
                }
            }
        });
    }

    public void moveShortcut(final Shortcut shortcut, final Category targetCategory) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for (Category category : getCategories()) {
                    category.getShortcuts().remove(shortcut);
                }
                targetCategory.getShortcuts().add(shortcut);
            }
        });
    }

    public void createCategory(final String name) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmList<Category> categories = getBase().getCategories();
                Category category = Category.createNew(name);
                category.setId(generateId(Category.class));
                category = realm.copyToRealm(category);
                categories.add(category);
            }
        });
    }

    public void renameCategory(final Category category, final String newName) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                category.setName(newName);
            }
        });
    }

    public void moveCategory(final Category category, final int position) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmList<Category> categories = getBase().getCategories();
                int oldPosition = categories.indexOf(category);
                categories.move(oldPosition, position);
            }
        });
    }

    public void deleteCategory(final Category category) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for (Shortcut shortcut : category.getShortcuts()) {
                    shortcut.getHeaders().deleteAllFromRealm();
                    shortcut.getParameters().deleteAllFromRealm();
                }
                category.getShortcuts().deleteAllFromRealm();
                category.deleteFromRealm();
            }
        });
    }

    public void deleteVariable(final Variable variable) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                variable.getOptions().deleteAllFromRealm();
                variable.deleteFromRealm();
            }
        });
    }

    public RealmResults<PendingExecution> getShortcutsPendingExecution() {
        return realm
                .where(PendingExecution.class)
                .findAllSorted(PendingExecution.FIELD_ENQUEUED_AT);
    }

    public void createPendingExecution(final long shortcutId, final List<ResolvedVariable> resolvedVariables) {
        long existingPendingExecutions = realm
                .where(PendingExecution.class)
                .equalTo(PendingExecution.FIELD_SHORTCUT_ID, shortcutId)
                .count();
        if (existingPendingExecutions == 0) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.copyToRealm(PendingExecution.createNew(shortcutId, resolvedVariables));
                }
            });
        }
    }

    public void removePendingExecution(final PendingExecution pendingExecution) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                pendingExecution.deleteFromRealm();
            }
        });
    }

    public Shortcut persist(final Shortcut shortcut) {
        if (shortcut.isNew()) {
            shortcut.setId(generateId(Shortcut.class));
        }

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(shortcut);
            }
        });
        return getShortcutById(shortcut.getId());
    }

    public Variable persist(final Variable variable) {
        final boolean isNew = variable.isNew();
        if (isNew) {
            variable.setId(generateId(Variable.class));
        }

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Variable newVariable = realm.copyToRealmOrUpdate(variable);
                if (isNew) {
                    getVariables().add(newVariable);
                }
            }
        });
        return getVariableById(variable.getId());
    }

    private long generateId(Class<? extends RealmObject> clazz) {
        Number maxId = realm.where(clazz).max(FIELD_ID);
        long maxIdLong = Math.max(maxId != null ? maxId.longValue() : 0, 0);
        return maxIdLong + 1;
    }

    public Collection<Shortcut> getShortcuts() {
        return realm.where(Shortcut.class).notEqualTo(FIELD_ID, Shortcut.TEMPORARY_ID).findAll();
    }
}
