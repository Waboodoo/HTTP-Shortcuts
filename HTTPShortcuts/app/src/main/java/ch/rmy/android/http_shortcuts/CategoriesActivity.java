package ch.rmy.android.http_shortcuts;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import ch.rmy.android.http_shortcuts.adapters.CategoryAdapter;
import ch.rmy.android.http_shortcuts.listeners.OnItemClickedListener;
import ch.rmy.android.http_shortcuts.realm.Controller;
import ch.rmy.android.http_shortcuts.realm.models.Category;
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

        controller = destroyer.own(new Controller(this));
        categories = controller.getCategories();
        adapter = destroyer.own(new CategoryAdapter(this));
        adapter.setParent(controller.getBase());

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
        List<String> options = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.category_menu_items)));
        if (categories.size() <= 1) {
            options.remove(options.size() - 1);
        }

        (new MaterialDialog.Builder(this))
                .title(category.getName())
                .items(options)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        performContextMenuAction(which, category);
                    }
                })
                .show();
    }

    private void performContextMenuAction(int action, Category category) {
        switch (action) {
            case 0:
                showRenameDialog(category);
                return;
            case 1:
                moveCategory(category, -1);
                return;
            case 2:
                moveCategory(category, 1);
                return;
            case 3:
                showDeleteDialog(category);
                return;
        }
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

    private void renameCategory(Category category, String newName) {
        controller.renameCategory(category, newName);
        showSnackbar(R.string.message_category_renamed);
    }

    private void moveCategory(Category category, int offset) {
        int position = categories.indexOf(category) + offset;
        if (position < 0 || position >= categories.size()) {
            return;
        }
        controller.moveCategory(category, position);
    }

    private void showDeleteDialog(final Category category) {
        (new MaterialDialog.Builder(this))
                .content(category.getShortcuts().isEmpty() ? R.string.confirm_delete_category_message : R.string.confirm_delete_non_empty_category_message)
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
