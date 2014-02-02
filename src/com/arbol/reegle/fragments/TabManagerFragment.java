package com.arbol.reegle.fragments;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.arbol.reegle.MainActivity;
import com.arbol.reegle.R;
import com.arbol.reegle.adapters.MainTabsAdapter;
import com.arbol.reegle.utility.ReegleDoc;
import com.arbol.reegle.utility.SearchManager;

import java.util.ArrayList;

/**
 * Created by user on 1/23/14.
 */
public class TabManagerFragment extends Fragment implements
        ActionBar.TabListener,
        SearchManager,
        MainTabsAdapter.FragmentReceiver {
    private MainTabsAdapter mainTabsAdapter;
    private FragmentManager fm;
    private ActionBarActivity activity;
    public ActionBar actionBar;
    private NewSearchFragment newSearchFragment;
    private SearchListFragment searchListFragment;
    private DocListFragment docListFragment;
    private Boolean fragmentsInstantiated = false;
    public static final String TAG = "manager_fragment";
    private static final String NOT_READY = "Oops! Something broke...";
    private static final String FRAGMENTS_LOST = "UI Error. Please restart app.";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (MainActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        renderTabs();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.activity = null;
    }

    /*
     * Custom Methods
     */

    private void renderTabs(){
        fm = activity.getSupportFragmentManager();
        mainTabsAdapter = new MainTabsAdapter(fm, this);

        actionBar = activity.getSupportActionBar();
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        ViewPager mPager = (ViewPager) activity.findViewById(R.id.main_slider);
        mPager.setAdapter(mainTabsAdapter);

        // When fragment swiped, notify ActionBar to select relevant tab
        mPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        // When swiping between pages, select the corresponding tab.
                        actionBar.setSelectedNavigationItem(position);
                    }
                }
        );

        // Add 3 tabs, specifying the tab's text and TabListener
        actionBar.addTab(actionBar.newTab().setText("New Search").setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText("Saved Searches").setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText("Results").setTabListener(this));
        actionBar.setSelectedNavigationItem(1);
    }

    /*
     * SearchManager Interface
     */

    @Override
    public void editSearch(Long search_id) {
        final Long SEARCH_ID = search_id;
        executeFragmentDependency(new FragmentDependency() {
            @Override
            public void success() {
                // Populate New Search Form Fields
                newSearchFragment.setForm(SEARCH_ID);
                // Go to New Search tab
                actionBar.setSelectedNavigationItem(MainTabsAdapter.INDEX_NEW_SEARCH_FRAGMENT);
            }
        });
    }

    @Override
    public void execSearch(Long search_id) {
        final Long SEARCH_ID = search_id;
        executeFragmentDependency(new FragmentDependency() {
            @Override
            public void success() {
                // Populate New Search Form Fields
                docListFragment.execSearch(SEARCH_ID);
                // Go to New Search tab
                actionBar.setSelectedNavigationItem(MainTabsAdapter.INDEX_DOC_LIST_FRAGMENT);
            }
        });
    }

    @Override
    public void addNewSearch() {
        executeFragmentDependency(new FragmentDependency() {
            @Override
            public void success() {
                // Clear Search Form Fields
                newSearchFragment.setForm(null);
                // Go to New Search tab
                actionBar.setSelectedNavigationItem(MainTabsAdapter.INDEX_NEW_SEARCH_FRAGMENT);
            }
        });
    }

    @Override
    public void searchSaved(Boolean bSaved) {
        final Boolean SAVED = bSaved;
        executeFragmentDependency(new FragmentDependency() {
            @Override
            public void success() {
                // reload list of searches if new search saved
                if (SAVED) {
                    searchListFragment.refresh();
                }
                // go to saved searches tab
                actionBar.setSelectedNavigationItem(MainTabsAdapter.INDEX_SEARCH_LIST_FRAGMENT);
            }
        });
    }

    @Override
    public void resultsReturned(ArrayList<ReegleDoc> reegleDocs) {
        final ArrayList<ReegleDoc> REEGLE_DOCS = reegleDocs;
        executeFragmentDependency(new FragmentDependency() {
            @Override
            public void success() {
                docListFragment.displayResults(REEGLE_DOCS);
            }
        });
    }

    @Override
    public void searchError(String err) {
        final String ERR = err;
        executeFragmentDependency(new FragmentDependency() {
            @Override
            public void success() {
                docListFragment.searchError(ERR);
            }
        });
    }

    /*
     * Implement TabListener
     */

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        if (activity != null){
            ViewPager mPager = (ViewPager) activity.findViewById(R.id.main_slider);
            mPager.setCurrentItem(tab.getPosition());
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // do nothing
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // do nothing
    }

    /*
     * Implement MainTabsAdapter.FragmentReceiver
     */

    @Override
    public void allFragmentsInstantiated(){
        fragmentsInstantiated = true;
        newSearchFragment = mainTabsAdapter.getNewSearchFragment();
        searchListFragment = mainTabsAdapter.getSearchListFragment();
        docListFragment = mainTabsAdapter.getDocListFragment();
    }

    /*
     * If ASYNC task interrupts allFragmentsInstantiated Call,
     *  try to manually get fragments. If not successfull tell
     *  user to restart app.
     */

    private Boolean checkFragments(){
        allFragmentsInstantiated();
        if (newSearchFragment == null && searchListFragment == null && docListFragment == null){
            fragmentsInstantiated = false;
        }
        return fragmentsInstantiated;
    }

    private void executeFragmentDependency(FragmentDependency fragmentDependency){
        if (fragmentsInstantiated){
            fragmentDependency.success();
        } else {
            try {
                for (Integer i=0; i<3; i++){
                    Thread.sleep(500);
                    if (checkFragments()){
                        fragmentDependency.success();
                        return;
                    }
                }
                ((MainActivity) activity).toaster(FRAGMENTS_LOST);
            } catch (InterruptedException e) {
                ((MainActivity) activity).toaster(FRAGMENTS_LOST);
                e.printStackTrace();
            }
        }
    }

    private interface FragmentDependency {
        public abstract void success();
    }
}
