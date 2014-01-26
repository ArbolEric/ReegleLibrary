package com.arbol.reegle.fragments;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import com.arbol.reegle.MainActivity;
import com.arbol.reegle.R;
import com.arbol.reegle.adapters.MainTabsAdapter;
import com.arbol.reegle.db.*;
import com.arbol.reegle.models.Country;
import com.arbol.reegle.models.Language;
import com.arbol.reegle.models.Search;
import com.arbol.reegle.models.Topic;
import com.arbol.reegle.utility.ListUtils;
import com.arbol.reegle.utility.MultiSelectionSpinner;
import com.arbol.reegle.utility.SearchManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 12/31/13.
 */
public class NewSearchFragment extends Fragment {
    private EditText vName;
    private MultiSelectionSpinner vCountries;
    private MultiSelectionSpinner vLanguages;
    private MultiSelectionSpinner vTopics;
    private TextView vSubTitle;
    private MySQLiteHelper dbHelper;
    private SQLiteDatabase database;
    private View view;
    private View save;
    private View cancel;
    private SearchManager searchManager;
    private Boolean bUpdate = false;
    private Long UPDATE_ID = null;
    private MainActivity activity;
    private Boolean initiated = false;

    private void setContext(){
        Log.d("FragmentInstances", "Setting Context");
        dbHelper = new MySQLiteHelper(activity);
        this.searchManager = this.activity.currentManagerFragment();
        if (initiated){
            setClickListeners();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retain this fragment across configuration changes.
        Log.d("Lifecycle", "recreating the whole fucking fragment");
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.new_search_fragment,
                    container, false);
        } else {
            ((ViewGroup) view.getParent()).removeView(view);
        };
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (MainActivity) activity;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        setContext();
        if (!initiated){
            initForm();
            fnClear();
            initiated = true;
            Log.d("Lifecycle", initiated.toString());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
        dbHelper = null;
        Log.d("Lifecycle", "detaching NewSearchFragment from activity");
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        Log.d("Lifecycle", "NewSearchFragment.onSaveInstanceState");
        outState.putBoolean("bInitiated", true);
    }

    /*
     * Custom Methods
     */

    private void initForm(){
        Log.d("Lifecycle", "Initializing form");
        save = view.findViewById(R.id.save_search);
        cancel = view.findViewById(R.id.cancel_new_search);
        database = dbHelper.getWritableDatabase();
        vName = (EditText)view.findViewById(R.id.search_name);
        vCountries = (MultiSelectionSpinner)view.findViewById(R.id.select_countries);
        vLanguages = (MultiSelectionSpinner)view.findViewById(R.id.select_languages);
        vTopics = (MultiSelectionSpinner)view.findViewById(R.id.select_topics);
        vSubTitle = (TextView)view.findViewById(R.id.new_search_subtitle);

        // Get countries, topics, and language options to populate MultiSelectionSpinner's
        List<String> aCountries = Country.listAll(database);
        List<String> aTopics = Topic.listAll(database);
        vCountries.setItems(aCountries);
        vTopics.setItems(aTopics);

        vLanguages.setItems(Language.listAll());
        database.close();

        setClickListeners();
    }

    private void setClickListeners(){
        Log.d("FragmentInstances", "NewSearchFragment => setting onClickListeners");
        // Save Values in Form to Search_Table
        save.setOnClickListener(saveListener());
        // clear form values
        cancel.setOnClickListener(cancelListener());
    }

    // Pre-Populate form and set bUpdate and UPDATE_ID for form submission.
    public void setForm(Long id){
        Log.d("Lifecycle", "setting from");
        if (id == null){  // Adding new search
            fnClear();
        } else {
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            Search search = Search.fnGet(id, database);
            database.close();
            vName.setText(search.name);
            vTopics.setSelection(search.listTopics());
            vCountries.setSelection(search.listCountries());
            vLanguages.setSelection(search.listLanguages());
            bUpdate = true;
            UPDATE_ID = id;
            vSubTitle.setText("You Are Editing: " + search.name);
        }
    }

    // clear form inputs
    private void fnClear(){
        Log.d("Lifecycle", "clearing form");
        // Clear form values.
        vName.setText("");
        vCountries.clearSelection();
        vTopics.clearSelection();
        List<String> aLanguages = Language.listAll();
        vLanguages.setSelection(aLanguages.indexOf("English"));
        // By default keep bUpdate false. only true after "Edit" selected for search.
        bUpdate = false;
        UPDATE_ID = null;
        vSubTitle.setText(R.string.new_search);
    }

    /*
     * Click Listeners
     */

    private View.OnClickListener saveListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database = dbHelper.getWritableDatabase();
                // Get Values from Form
                String sName = vName.getText().toString();
                List<String> aCountries = vCountries.getSelectedStrings();
                List<String> aTopics = vTopics.getSelectedStrings();
                List<String> aLanguageNames = vLanguages.getSelectedStrings();

                // Validate Form Values
                if (sName.length() == 0) {
                    activity.toaster("You must name your new search!!");
                } else if ((aCountries.size() + aTopics.size() + aLanguageNames.size()) == 0) {
                    activity.toaster("You must select at least one criteria for your search!");
                } else {
                    // Check to see if Search Name already Exists in DB
                    String checkName;
                    if (bUpdate){
                        checkName = String.format("SELECT count() FROM %s WHERE %s ='%s' AND %s!=%s",
                                Search_Table.TABLE_SEARCH, Search_Table.COLUMN_DISPLAY, sName,
                                Search_Table.COLUMN_ID, UPDATE_ID
                        );
                    } else {
                        checkName = String.format("SELECT count() FROM %s WHERE %s ='%s'",
                                Search_Table.TABLE_SEARCH, Search_Table.COLUMN_DISPLAY, sName
                        );
                    }
                    Cursor count = database.rawQuery(checkName, null);
                    count.moveToFirst();
                    if (count.getInt(0) == 0){
                        ContentValues values = new ContentValues();
                        values.put(Search_Table.COLUMN_DISPLAY, sName);
                        values.put(Search_Table.COLUMN_LANGUAGES, ListUtils.join(aLanguageNames, ", "));
                        values.put(Search_Table.COLUMN_TOPICS, ListUtils.join(aTopics, ", "));
                        values.put(Search_Table.COLUMN_COUNTRIES, ListUtils.join(aCountries, ", "));
                        if (bUpdate){
                            // Update Existing Search
                            Search search = Search.fnGet(UPDATE_ID, database);
                            search.update(values, database);
                            searchManager.searchSaved(true);
                        } else {
                            // Create New Search
                            Search.fnCreate(values, database);
                            searchManager.searchSaved(true);
                        }
                        fnClear();
                    } else {
                        activity.toaster("Your search must have a unique name.");
                    }
                    count.close();
                }
                database.close();
            }
        };
    }

    private View.OnClickListener cancelListener(){
       return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fnClear();
                searchManager.searchSaved(false);
            }
       };
    }
}