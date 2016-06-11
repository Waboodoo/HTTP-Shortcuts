package ch.rmy.android.http_shortcuts;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import ch.rmy.android.http_shortcuts.realm.models.Category;

public class CategoryPagerAdapter extends FragmentPagerAdapter {

    private final FragmentManager fragmentManager;

    private List<ListFragment> fragments = new ArrayList<>();

    public CategoryPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
        this.fragmentManager = fragmentManager;
    }

    public void setCategories(List<Category> categories, boolean shortcutPlacementMode) {
        for (int i = fragments.size(); i < categories.size(); i++) {
            Fragment restoredFragment = fragmentManager.findFragmentByTag(makeFragmentName(i));
            if (restoredFragment != null && restoredFragment instanceof ListFragment) {
                fragments.add((ListFragment) restoredFragment);
            } else {
                fragments.add(new ListFragment());
            }
        }

        for (int i = 0; i < fragments.size(); i++) {
            ListFragment fragment = fragments.get(i);
            fragment.setCategoryId(categories.get(i).getId());
            fragment.setShortcutPlacementMode(shortcutPlacementMode);
        }
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
        return fragments.get(position).getCategoryName();
    }

    private static String makeFragmentName(int position) {
        return "android:switcher:" + R.id.view_pager + ":" + position;
    }

}