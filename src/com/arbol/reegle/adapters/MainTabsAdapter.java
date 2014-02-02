package com.arbol.reegle.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;
import com.arbol.reegle.fragments.DocListFragment;
import com.arbol.reegle.fragments.NewSearchFragment;
import com.arbol.reegle.fragments.SearchListFragment;

/**
 * Created by user on 1/21/14.
 */
public class MainTabsAdapter extends FragmentPagerAdapter {

    static private final Integer NUM = 3;
    static public final Integer INDEX_NEW_SEARCH_FRAGMENT = 0;
    static public final Integer INDEX_SEARCH_LIST_FRAGMENT = 1;
    static public final Integer INDEX_DOC_LIST_FRAGMENT = 2;
    private NewSearchFragment newSearchFragment;
    private SearchListFragment searchListFragment;
    private DocListFragment docListFragment;
    private SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();
    private FragmentReceiver fragmentReceiver;

    public MainTabsAdapter(FragmentManager fm, FragmentReceiver fragmentReceiver){
        super(fm);
        this.fragmentReceiver = fragmentReceiver;
    }

    public void setContext(FragmentReceiver fragmentReceiver){
        this.fragmentReceiver = fragmentReceiver;
    }

    public interface FragmentReceiver {
        public void allFragmentsInstantiated();
    }

    @Override
    public Fragment getItem(int i) {
        switch (i){
            case (0): {
                newSearchFragment = new NewSearchFragment();
                return newSearchFragment;
            } case (1): {
                searchListFragment = new SearchListFragment();
                return searchListFragment;
            } default: {
                docListFragment = new DocListFragment();
                return docListFragment;
            }
        }
    }

    @Override
    public int getCount() {
        return NUM;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case (0): {
                return "New Search";
            } case (1): {
                return "Saved Searches";
            } default: {
                return "Results";
            }
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        if (registeredFragments.size() == NUM) {
            fragmentReceiver.allFragmentsInstantiated();
        }
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        super.destroyItem(container, position, object);
    }

    private Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }

    public NewSearchFragment getNewSearchFragment(){
        return (NewSearchFragment) getRegisteredFragment(INDEX_NEW_SEARCH_FRAGMENT);
    }

    public SearchListFragment getSearchListFragment(){
        return (SearchListFragment) getRegisteredFragment(INDEX_SEARCH_LIST_FRAGMENT);
    }

    public DocListFragment getDocListFragment(){
        return (DocListFragment) getRegisteredFragment(INDEX_DOC_LIST_FRAGMENT);
    }
}
