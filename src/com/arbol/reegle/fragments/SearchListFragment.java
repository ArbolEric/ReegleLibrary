package com.arbol.reegle.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.*;
import android.widget.ExpandableListView;
import com.arbol.reegle.MainActivity;
import com.arbol.reegle.R;
import com.arbol.reegle.adapters.SearchAdapter;
import com.arbol.reegle.utility.SearchManager;

/**
 * Created by user on 12/31/13.
 */
public class SearchListFragment extends Fragment {
    private View view;
    private SearchAdapter mAdapter;
    private MainActivity activity;
    private SearchManager searchManager;

    public void setContext(){
        this.searchManager = activity.currentManagerFragment();
        if (mAdapter != null) {
            mAdapter.setContext(activity);
        }
        view.findViewById(R.id.add_search).setOnClickListener(addSearchListener());
        view.findViewById(R.id.favorite_list_item).setOnClickListener(displayFavoritesListener());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retain this fragment across configuration changes.
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.search_list_fragment,
                container, false);
        setContext();
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (MainActivity) activity;
    }

    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        initSearchList();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
    }

    /*
     * Custom Methods
     */

    private void initSearchList(){
        mAdapter = new SearchAdapter(activity);
        ExpandableListView lv = (ExpandableListView) view.findViewById(R.id.search_list);
        // Set up our adapter
        lv.setAdapter(mAdapter);
        lv.setEmptyView(view.findViewById(R.id.empty_search_list));


    }

    // refresh listview items
    public void refresh(){
        mAdapter.refresh();
    }

    /*
     * Click Listeners
     */

    private View.OnClickListener displayFavoritesListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchManager.execSearch(null);
            }
        };
    }
    private View.OnClickListener addSearchListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchManager.addNewSearch();
            }
        };
    }
}