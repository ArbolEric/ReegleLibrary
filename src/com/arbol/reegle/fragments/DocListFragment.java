package com.arbol.reegle.fragments;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import com.arbol.reegle.MainActivity;
import com.arbol.reegle.R;
import com.arbol.reegle.db.MySQLiteHelper;
import com.arbol.reegle.models.Search;
import com.arbol.reegle.adapters.DocumentAdapter;
import com.arbol.reegle.utility.ReegleDoc;

import java.util.ArrayList;

/**
 * Created by user on 12/31/13.
 */
public class DocListFragment extends Fragment {
    private View view;
    private DocumentAdapter mAdapter;
    private ListView lv;
    private MySQLiteHelper dbHelper;
    private Boolean initiated = false;
    private MainActivity activity;
    private Search.ExecSearch pendingSearch;

    private void setContext(MainActivity activity){
        dbHelper = new MySQLiteHelper(activity);
        if (mAdapter != null) {
            mAdapter.setContext(activity);
        }
        if (pendingSearch != null){
            pendingSearch.setContext(activity);
        }
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
        if (view == null){
            view = inflater.inflate(R.layout.doc_list_fragment,
                container, false);
        } else {
            ((ViewGroup) view.getParent()).removeView(view);
        }
        setContext(activity);
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (MainActivity) activity;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!initiated){
            initDocList();
        }
        if (pendingSearch != null){
            pendingSearch.setContext(activity);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
        dbHelper = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
    }

    /*
     * Custom Methods
     */

    private void initDocList(){
        mAdapter = new DocumentAdapter(activity);;
        ArrayList<ReegleDoc> docs;
        execSearch(null);
        lv = (ListView) view.findViewById(R.id.doc_list);
        lv.setAdapter(mAdapter);
        lv.setEmptyView(view.findViewById(R.id.empty_doc_list));

        initiated = true;
    }

    public void execSearch(Long searchId) {
        if (pendingSearch != null) {
            pendingSearch.cancel(true);
        }
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        TextView title = (TextView) view.findViewById(R.id.search_title);
        if (searchId == null) {
            mAdapter.showFavorites(database);
            title.setText(R.string.favorites);
            fnUnWait();
            database.close();
        } else {
            fnWait();
            Search search = Search.fnGet(searchId, database);
            pendingSearch = search.exec(database, activity);
            title.setText(search.name);
        }
    }

    public void displayResults(ArrayList<ReegleDoc> reegleDocs) {
        Log.d("Arbol", "docListFragment.displayResults");
        mAdapter.setResults(reegleDocs);
        fnUnWait();
    }

    private void fnWait(){
        view.findViewById(R.id.wait_for_search).setVisibility(View.VISIBLE);
    }

    private void fnUnWait(){
        Log.d("Arbol", "docListFragment.fnUnWait");
        Log.d("Arbol", view.findViewById(R.id.wait_for_search).toString());
        view.findViewById(R.id.wait_for_search).setVisibility(View.GONE);
    }
}
