package com.arbol.reegle.fragments;

/*
 * After database has been created, fill Reegle Tables with data from CSV and API
 */

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.arbol.reegle.MainActivity;
import com.arbol.reegle.R;
import com.arbol.reegle.db.MySQLiteHelper;
import com.arbol.reegle.db.Reegle_Country_Table;
import com.arbol.reegle.db.Reegle_Topic_Table;
import com.arbol.reegle.utility.ListUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: user
 * Date: 12/7/13
 * Time: 1:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class ReegleStuffFragment extends Fragment {
    private MainActivity activity;
    private Boolean initiated = false;
    private View view;
    public static final String TAG = "reegle_stuff_fragment";

    public interface ReegleStuffListener {
        public void dataStuffed();
        public void reegleStuffError(String s);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view == null){
            view = inflater.inflate(R.layout.reegle_db_stuff,
                    container, false);
        } else {
            ((ViewGroup) view.getParent()).removeView(view);
        }
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
        Log.d("SearchTask", "ReegleStuffFragment onActivityCreated");
        setRetryListener();
        if (!initiated) {
            stuffReegleData();
            initiated = true;
        }
    }

    public void stuffReegleData(){
        if (!activity.isNetworkAvailable()){
            reegleStuffError(activity.getString(R.string.no_internet_connection));
            return;
        }
        MySQLiteHelper dbHelper = new MySQLiteHelper(activity);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        String dropCountries = String.format("DROP TABLE IF EXISTS %s;", Reegle_Country_Table.TABLE_NAME);
        String dropTopics = String.format("DROP TABLE IF EXISTS %s;", Reegle_Topic_Table.TABLE_NAME);
        database.execSQL(dropCountries);
        database.execSQL(dropTopics);
        database.execSQL(Reegle_Country_Table.CREATE);
        database.execSQL(Reegle_Topic_Table.CREATE);
        database.close();

        stuffCountries();
        stuffTopics();
    }

    // WRITE COUNTRIES TO DATABASE FROM CSV FILE
    private void stuffCountries(){
        MySQLiteHelper dbHelper = new MySQLiteHelper(activity);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        InputStream inputStream = activity.getResources().openRawResource(R.raw.reeglecountries);
        InputStreamReader inputBuffer = new InputStreamReader(inputStream);
        BufferedReader reader = new BufferedReader(inputBuffer);

        database.beginTransaction();
        try {
            String str1 = String.format(
                    "INSERT INTO %s (%s) VALUES ",
                    Reegle_Country_Table.TABLE_NAME,
                    ListUtils.join(Reegle_Country_Table.COUNTRIES_COLUMNS, ",")
            );
            StringBuilder sb = new StringBuilder(str1);
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.replaceAll("'","''");
                String[] str = line.split(",");
                sb.append("('" + str[0] + "','");
                sb.append(str[1] + "','");
                sb.append(str[2] + "'),");
            }
            sb.replace(sb.length()-1, sb.length(), "");
            sb.append(";");
            database.execSQL(sb.toString());
        } catch (IOException e){
            e.getMessage();
        }
        database.setTransactionSuccessful();
        database.endTransaction();
        database.close();
    }
    private void stuffTopics(){
        new StuffTopics().execute();
    }

    private class StuffTopics extends AsyncTask<Void, String, Void> {
        private Boolean error = false;

        @Override
        protected Void doInBackground(Void... params) {
            Log.d("Arbol", "Exectuting stuffTopics");
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost("http://poolparty.reegle.info/PoolParty/sparql/glossary?format=application/json");
                String SPARQL = "PREFIX skos:<http://www.w3.org/2004/02/skos/core#>" +
                        "SELECT ?topicUri ?topicLabel {" +
                        " ?scheme skos:hasTopConcept ?topicUri." +
                        " ?topicUri skos:prefLabel ?topicLabel." +
                        " FILTER(lang(?topicLabel) = \"en\")" +
                        "}";
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("query", SPARQL));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpclient.execute(httppost);
                String responseBody = EntityUtils.toString(response.getEntity());
                JSONTokener tokener = new JSONTokener(responseBody);
                JSONObject json = new JSONObject(tokener);
                JSONArray bindings = json.getJSONObject("results").getJSONArray("bindings");
                String tableName = Reegle_Topic_Table.TABLE_NAME;
                String columns = ListUtils.join(Reegle_Topic_Table.TOPICS_COLUMNS, ",");
                MySQLiteHelper dbHelper = new MySQLiteHelper(activity);
                SQLiteDatabase database = dbHelper.getWritableDatabase();
                database.beginTransaction();
                String str1 = "INSERT INTO " + tableName + " (" + columns + ") VALUES ";
                StringBuilder sb = new StringBuilder(str1);
                Integer l = bindings.length();
                for (Integer i=0; i<l; i++){
                    JSONObject oTopic = bindings.getJSONObject(i);
                    JSONObject topic = oTopic.getJSONObject("topicUri");
                    JSONObject label = oTopic.getJSONObject("topicLabel");
                    String sTopic = topic.getString("value");
                    String sLabel = label.getString("value");
                    sTopic = sTopic.replaceAll("'", "''");
                    sLabel = sLabel.replaceAll("'", "''");
                    sb.append("('" + sTopic + "','" + sLabel + "'),");
                }
                sb.replace(sb.length()-1, sb.length(), "");
                sb.append(";");
                database.execSQL(sb.toString());
                database.setTransactionSuccessful();
                database.endTransaction();
                database.close();
            } catch (ClientProtocolException e) {
                error = true;
                reegleStuffError("We had trouble downloading Reegle search criteria.");
                e.printStackTrace();
            } catch (IOException e) {
                error = true;
                reegleStuffError("We had trouble downloading Reegle search criteria.");
                e.printStackTrace();
            } catch (JSONException e) {
                error = true;
                reegleStuffError("We had trouble reading the Reegle search criteria.");
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void x) {
            if (!error){
                // Alert MainActivity that data has been stuffed and it should resume
                activity.dataStuffed();
            }
        }
    }

    private void reegleStuffError (String err){
        activity.reegleStuffError(err);
    }

    public void setRetryListener(){
        activity.findViewById(R.id.retry_reegle_stuff).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.findViewById(R.id.reegle_stuff_error).setVisibility(View.GONE);
                activity.findViewById(R.id.wait_for_reegle_stuff).setVisibility(View.VISIBLE);
                stuffReegleData();
            }
        });
    }
}

