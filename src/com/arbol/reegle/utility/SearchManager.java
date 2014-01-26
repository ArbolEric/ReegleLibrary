package com.arbol.reegle.utility;

import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by user on 1/24/14.
 */
public abstract interface SearchManager {

    public abstract void addNewSearch();
    public abstract void searchSaved(Boolean bSaved);
    public abstract void resultsReturned(ArrayList<ReegleDoc> reegleDocs);
    public abstract void searchError(String s);
    public abstract void editSearch(Long search_id);
    public abstract void execSearch(Long search_id);


}
