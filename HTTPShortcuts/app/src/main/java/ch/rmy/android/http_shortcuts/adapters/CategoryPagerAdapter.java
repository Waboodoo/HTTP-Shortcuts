package ch.rmy.android.http_shortcuts.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import ch.rmy.android.http_shortcuts.ListFragment;
import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.realm.models.Category;
import ch.rmy.android.http_shortcuts.utils.SelectionMode;

public class CategoryPagerAdapter extends FragmentPagerAdapter {

    private final FragmentManager fragmentManager;

    private List<ListFragment> fragments = new ArrayList<>();
    private List<String> names = new ArrayList<>();

    public CategoryPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
        this.fragmentManager = fragmentManager;
    }

    public void setCategories(List<Category> categories, SelectionMode selectionMode) {
        for (int i = fragments.size(); i < categories.size(); i++) {
            Fragment restoredFragment = fragmentManager.findFragmentByTag(makeFragmentName(i));
            if (restoredFragment != null && restoredFragment instanceof ListFragment) {
                fragments.add((ListFragment) restoredFragment);
            } else {
                fragments.add(new ListFragment());
            }
        }

        while (fragments.size() > categories.size()) {
            fragments.remove(fragments.size() - 1);
        }

        names.clear();
        for (int i = 0; i < fragments.size(); i++) {
            Category category = categories.get(i);
            ListFragment fragment = fragments.get(i);
            fragment.setCategoryId(category.getId());
            fragment.setSelectionMode(selectionMode);
            names.add(category.getName());
        }
        notifyDataSetChanged();
    }

    @Override
    public ListFragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return names.get(position);
    }

    private static String makeFragmentName(int position) {
        return "android:switcher:" + R.id.view_pager + ":" + position;
    }

}