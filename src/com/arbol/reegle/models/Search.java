package com.arbol.reegle.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import com.arbol.reegle.MainActivity;
import com.arbol.reegle.db.Search_Table;
import com.arbol.reegle.fragments.DocListFragment;
import com.arbol.reegle.utility.ReegleDoc;
import com.arbol.reegle.utility.SearchManager;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: user
 * Date: 11/21/13
 * Time: 8:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class Search {

    /*
     * Class Attributes and Methods
     */
    static public ArrayList<ContentValues> all(SQLiteDatabase database){
        Cursor mCursor = database.query(Search_Table.TABLE_SEARCH, null, null, null, null, null, Search_Table.COLUMN_DISPLAY);
        mCursor.moveToFirst();
        ArrayList<ContentValues> aValues = new ArrayList<ContentValues>();
        while (!mCursor.isAfterLast()){
            ContentValues rowValues = new ContentValues();
            DatabaseUtils.cursorRowToContentValues(mCursor, rowValues);
            aValues.add(rowValues);
            mCursor.moveToNext();
        }
        mCursor.close();
        return aValues;
    }

    // Save new Search to database
    static public Search fnCreate(ContentValues values, SQLiteDatabase database) {
        Long id = database.insert(Search_Table.TABLE_SEARCH, Search_Table.COLUMNS.toString(), values);
        values.put(Search_Table.COLUMN_ID, id);
        return new Search(values);
    }

    static public Search fnGet(Long id, SQLiteDatabase database){
        Cursor mCursor = database.query(Search_Table.TABLE_SEARCH, null,
                Search_Table.COLUMN_ID + "=?",
                new String[]{Long.toString(id)},
                null, null, null
        );
        mCursor.moveToFirst();
        ContentValues values = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(mCursor, values);
        mCursor.close();
        return new Search(values);
    }

    static public boolean fnDelete(Long id, SQLiteDatabase database){
        String where = String.format("%s=?",
                Search_Table.COLUMN_ID
        );
        int i = database.delete(Search_Table.TABLE_SEARCH, where, new String[]{Long.toString(id)});
        if (i == 0){
            return false;
        }
        return true;
    }

    /*
     * Instance Attributes
     */

    public static final String DOCCT = "50";
    private static final String TOKEN = "e37ed0340e534c5da2bdd60dce486abb";
    public Long id;
    public String name;
    public String languages;
    public String topics;
    public String countries;

    /*
     * Public Constructor
     */

    // Save New Search
    public Search(ContentValues values){
        this.id = values.getAsLong(Search_Table.COLUMN_ID);
        this.setValues(values);
    }

    /*
     * Value Parsing and Updating Methods
     */

    public void setValues(ContentValues values){
        this.name = values.getAsString(Search_Table.COLUMN_DISPLAY);
        this.languages = values.getAsString(Search_Table.COLUMN_LANGUAGES);
        this.topics = values.getAsString(Search_Table.COLUMN_TOPICS);
        this.countries = values.getAsString(Search_Table.COLUMN_COUNTRIES);
    }

    public ContentValues toValues(){
        ContentValues values = new ContentValues();
        values.put(Search_Table.COLUMN_DISPLAY, name);
        values.put(Search_Table.COLUMN_LANGUAGES, languages);
        values.put(Search_Table.COLUMN_COUNTRIES, countries);
        values.put(Search_Table.COLUMN_TOPICS, topics);
        return values;
    }

    public void update(ContentValues values, SQLiteDatabase database){
        this.setValues(values);
        this.save(database);
    }

    // Save changes to database
    private void save(SQLiteDatabase database) {
        String where = Search_Table.COLUMN_ID + "=?";
        database.update(Search_Table.TABLE_SEARCH, this.toValues(), where, new String[]{Long.toString(id)});
    }

    /*
     * Output as Lists (String[])
     */

    public String[] listTopics(){
        return topics.split(", ");
    }
    public String[] listCountries(){
        return countries.split(", ");
    }
    public String[] listLanguages(){
        return languages.split(", ");
    }

    /*
     * Instance URL Construction
     */

    public String toUrl(SQLiteDatabase database) {
        // calculate url
        String link =  String.format("http://api.reegle.info/service/recommend?" +
                "token=%s&" +
                "filterLocales=%s&" +
                "countDocuments=%s&" +
                "filterTopics=%s" +
                "&filterCountries=%s",
                TOKEN,
                Language.getCodes(languages),
                DOCCT,
                Topic.getCodes(database, topics),
                Country.getCodes(database, countries)
        );
        return link;
    }
    /*
     * Execute Search
     */

    public ExecSearch exec (SQLiteDatabase database, MainActivity activity) {
        if (TOKEN == null){
            activity.currentManagerFragment().searchError("No authorized Reegle Token.");
            return null;
        }
        if (!activity.isNetworkAvailable())  {
            activity.currentManagerFragment().searchError("You need to connect to the Internet.");
            return null;
        }
        ExecSearch e = new ExecSearch(database, activity);
        e.execute();
        return e;
    }

    public class ExecSearch extends AsyncTask<Void, String, Void> {
        private static final String SEARCH_ERROR = "Error downloading results from Reegle.";
        private String search_error;
        private String url;
        private ArrayList<ReegleDoc> reegleDocs;
        private MainActivity activity;
        public Boolean isComplete = false;

        public ExecSearch(SQLiteDatabase database, MainActivity activity){
            this.activity = activity;
            url = toUrl(database);
            database.close();
        }

        public void setContext(MainActivity current_activity){
            this.activity = current_activity;
        }

        @Override
        protected Void doInBackground(Void... params) {
            reegleDocs = new ArrayList<ReegleDoc>();
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet request = new HttpGet(url);

            request.setHeader("Accept",
                    "application/json");
            try {
                HttpResponse response = httpclient.execute(request);
                BufferedReader rd = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent()));

                StringBuffer result = new StringBuffer();
                String line = "";
                while ((line = rd.readLine()) != null) {
                    if (isCancelled()) return null;
                    result.append(line);
                }
                if (isCancelled()) return null;
                reegleDocs = parseResults(result);
            }  catch (IOException e) {
                search_error = SEARCH_ERROR;
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void x) {
            if (search_error != null){
                activity.currentManagerFragment().searchError(search_error);
            } else {
                activity.currentManagerFragment().resultsReturned(reegleDocs);
            }
            isComplete = true;
        }

        private ArrayList<ReegleDoc> parseResults(StringBuffer result){
            ArrayList<ReegleDoc> docs = new ArrayList<ReegleDoc>();
            try {
                JSONObject json = new JSONObject(result.toString());
                JSONArray aDocs = json.getJSONArray("documents");
                Integer l = aDocs.length();
                for (Integer i=0; i<l; i++){
                    if (isCancelled()) return null;
                    docs.add(new ReegleDoc(aDocs.getJSONObject(i)));
                }
                return docs;
            } catch (JSONException e) {
                search_error = SEARCH_ERROR;
                return null;
            }
        }

    }

}
