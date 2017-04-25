package ch.rmy.android.http_shortcuts.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import butterknife.Bind;
import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.adapters.CategoryAdapter;
import ch.rmy.android.http_shortcuts.listeners.OnItemClickedListener;
import ch.rmy.android.http_shortcuts.realm.Controller;
import ch.rmy.android.http_shortcuts.realm.models.Category;
import ch.rmy.android.http_shortcuts.utils.MenuDialogBuilder;
import ch.rmy.android.http_shortcuts.utils.ShortcutListDecorator;
import io.realm.RealmList;

public class CategoriesActivity extends BaseActivity {

    private static final int NAME_MIN_LENGTH = 1;
    private static final int NAME_MAX_LENGTH = 20;

    @Bind(R.id.category_list)
    RecyclerView categoryList;
    @Bind(R.id.button_create_category)
    FloatingActionButton createButton;

    private Controller controller;
    private RealmList<Category> categories;
    private CategoryAdapter adapter;

    private OnItemClickedListener<Category> clickedListener = new OnItemClickedListener<Category>() {
        @Override
        public void onItemClicked(Category category) {
            showContextMenu(category);
        }

        @Override
        public void onItemLongClicked(Category category) {
            showContextMenu(category);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        controller = destroyer.own(new Controller());
        categories = controller.getCategories();
        adapter = destroyer.own(new CategoryAdapter(this));
        adapter.setItems(controller.getBase().getCategories());

        LinearLayoutManager manager = new LinearLayoutManager(this);
        categoryList.setLayoutManager(manager);
        categoryList.setHasFixedSize(true);
        categoryList.addItemDecoration(new ShortcutListDecorator(this, R.drawable.list_divider));
        categoryList.setAdapter(adapter);

        adapter.setOnItemClickListener(clickedListener);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCreateDialog();
            }
        });
    }

    private void openCreateDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.title_create_category)
                .inputRange(NAME_MIN_LENGTH, NAME_MAX_LENGTH)
                .input(getString(R.string.placeholder_category_name), null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        createCategory(input.toString());
                    }
                }).show();
    }

    private void createCategory(String name) {
        controller.createCategory(name);
        showSnackbar(R.string.message_category_created);
    }

    private void showContextMenu(final Category category) {
        MenuDialogBuilder builder = new MenuDialogBuilder(this)
                .title(category.getName());

        builder.item(R.string.action_rename, new MenuDialogBuilder.Action() {
            @Override
            public void execute() {
                showRenameDialog(category);
            }
        });
        builder.item(R.string.action_change_category_layout_type, new MenuDialogBuilder.Action() {
            @Override
            public void execute() {
                showLayoutTypeDialog(category);
            }
        });
        if (canMoveCategory(category, -1)) {
            builder.item(R.string.action_move_up, new MenuDialogBuilder.Action() {
                @Override
                public void execute() {
                    moveCategory(category, -1);
                }
            });
        }
        if (canMoveCategory(category, 1)) {
            builder.item(R.string.action_move_down, new MenuDialogBuilder.Action() {
                @Override
                public void execute() {
                    moveCategory(category, 1);
                }
            });
        }
        if (categories.size() > 1) {
            builder.item(R.string.action_delete, new MenuDialogBuilder.Action() {
                @Override
                public void execute() {
                    showDeleteDialog(category);
                }
            });
        }

        builder.show();
    }

    private void showRenameDialog(final Category category) {
        new MaterialDialog.Builder(this)
                .title(R.string.title_rename_category)
                .inputRange(NAME_MIN_LENGTH, NAME_MAX_LENGTH)
                .input(getString(R.string.placeholder_category_name), category.getName(), new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        renameCategory(category, input.toString());
                    }
                }).show();
    }

    private void showLayoutTypeDialog(final Category category) {
        new MenuDialogBuilder(this)
                .item(R.string.layout_type_linear_list, new MenuDialogBuilder.Action() {
                    @Override
                    public void execute() {
                        changeLayoutType(category, Category.LAYOUT_LINEAR_LIST);
                    }
                })
                .item(R.string.layout_type_grid, new MenuDialogBuilder.Action() {
                    @Override
                    public void execute() {
                        changeLayoutType(category, Category.LAYOUT_GRID);
                    }
                })
                .show();
    }

    private void renameCategory(Category category, String newName) {
        controller.renameCategory(category, newName);
        showSnackbar(R.string.message_category_renamed);
    }

    private void changeLayoutType(Category category, String layoutType) {
        controller.setLayoutType(category, layoutType);
        showSnackbar(R.string.message_layout_type_changed);
    }

    private boolean canMoveCategory(Category category, int offset) {
        int position = categories.indexOf(category) + offset;
        return position >= 0 && position < categories.size();
    }

    private void moveCategory(Category category, int offset) {
        if (!canMoveCategory(category, offset)) {
            return;
        }
        int position = categories.indexOf(category) + offset;
        controller.moveCategory(category, position);
    }

    private void showDeleteDialog(final Category category) {
        if (category.getShortcuts().isEmpty()) {
            deleteCategory(category);
            return;
        }
        (new MaterialDialog.Builder(this))
                .content(R.string.confirm_delete_category_message)
                .positiveText(R.string.dialog_delete)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        deleteCategory(category);
                    }
                })
                .negativeText(R.string.dialog_cancel)
                .show();
    }

    private void deleteCategory(Category category) {
        controller.deleteCategory(category);
        showSnackbar(R.string.message_category_deleted);
    }

}
