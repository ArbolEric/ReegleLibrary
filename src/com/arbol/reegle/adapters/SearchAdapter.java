package com.arbol.reegle.adapters;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.arbol.reegle.MainActivity;
import com.arbol.reegle.db.*;
import com.arbol.reegle.R;
import com.arbol.reegle.models.Search;
import com.arbol.reegle.utility.SearchManager;

import java.util.List;

public class SearchAdapter extends BaseExpandableListAdapter {
    private List<ContentValues> aValues;
    private MainActivity activity;
    private SearchManager searchManager;

    public SearchAdapter(MainActivity activity) {
        super();
        setContext(activity);
        getData();
    }

    public void setContext(MainActivity activity){
        this.activity = activity;
        this.searchManager = activity.currentManagerFragment();
    }

    public void refresh(){
        getData();
        notifyDataSetChanged();
    }

    private void getData(){
        MySQLiteHelper dbHelper = new MySQLiteHelper(activity);
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        aValues = Search.all(database);
        database.close();
    }

    @Override
    public boolean isEmpty(){
        if (aValues.size() == 0){
            return true;
        }
        return false;
    }

    @Override
    public int getGroupCount() {
        return aValues.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return aValues.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return getGroup(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return aValues.get(groupPosition).getAsLong(Search_Table.COLUMN_ID);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        NameHolder nameHolder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(activity);
            convertView = inflater.inflate(R.layout.search_item, null);
            ViewGroup viewGroup = (ViewGroup) convertView;
            nameHolder = new NameHolder();
            nameHolder.name = (TextView) viewGroup.findViewById(R.id.search_name);//convertView.findViewById(R.id.search_name);
            nameHolder.exec_search = (ImageView) viewGroup.findViewById(R.id.exec_search);
            convertView.setTag(nameHolder);
        } else {
            nameHolder = (NameHolder) convertView.getTag();
        }
        ContentValues values = aValues.get(groupPosition);
        nameHolder.exec_search.setTag(values.getAsLong(Search_Table.COLUMN_ID));
        nameHolder.name.setText(values.getAsString(Search_Table.COLUMN_DISPLAY));

        // Listener to open search results in DocListFragment
        nameHolder.exec_search.setOnClickListener(execSearchListener);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View detailsView = convertView;
        DetailsHolder detailsHolder;
        ContentValues values = aValues.get(groupPosition);
        if (detailsView == null) {
            LayoutInflater inflater = LayoutInflater.from(activity);
            detailsView = inflater.inflate(R.layout.search_details, null);
            detailsHolder = new DetailsHolder();
            detailsHolder.topics = (TextView) detailsView.findViewById(R.id.topics_values);
            detailsHolder.countries = (TextView) detailsView.findViewById(R.id.countries_values);
            detailsHolder.languages = (TextView) detailsView.findViewById(R.id.languages_values);
            detailsHolder.edit = (ImageView) detailsView.findViewById(R.id.edit_search);
            detailsHolder.remove = (ImageView) detailsView.findViewById(R.id.delete_search);
            detailsView.setTag(detailsHolder);
        } else {
            detailsHolder = (DetailsHolder) detailsView.getTag();
        }
        detailsHolder.edit.setTag(values.getAsLong(Search_Table.COLUMN_ID));
        detailsHolder.remove.setTag(values.getAsLong(Search_Table.COLUMN_ID));

        detailsHolder.countries.setText(values.getAsString(Search_Table.COLUMN_COUNTRIES));
        detailsHolder.topics.setText(values.getAsString(Search_Table.COLUMN_TOPICS));
        detailsHolder.languages.setText(values.getAsString(Search_Table.COLUMN_LANGUAGES));

        // Open this search up in the editor.
        detailsHolder.edit.setOnClickListener(editSearchListener);
        // Remove from database and refresh when Remove button clicked.
        detailsHolder.remove.setOnClickListener(deleteSearchListener);
        return detailsView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    /*
     * View Holders
     */

    private static class DetailsHolder {
        public TextView languages;
        public TextView topics;
        public TextView countries;
        public ImageView edit;
        public ImageView remove;
    }

    private static class NameHolder {
        public TextView name;
        public ImageView exec_search;
    }

    /*
     * Click Listeners
     */
    private final View.OnClickListener execSearchListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Long search_id = (Long) v.getTag();
            searchManager.execSearch(search_id);
        }
    };

    private final View.OnClickListener editSearchListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Long search_id = (Long) v.getTag();
            searchManager.editSearch(search_id);
        }
    };

    private final View.OnClickListener deleteSearchListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // First confirm that they want to delete it
            // Build dialog message
            final Long search_id = (Long) v.getTag();
            MySQLiteHelper dbHelper = new MySQLiteHelper(activity);
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            Search search = Search.fnGet(search_id, database);
            database.close();
            CharSequence msg = String.format(activity.getResources().getString(R.string.confirm_delete_search),
                    search.name);
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage(msg)
                    // confirmed
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            MySQLiteHelper dbHelper = new MySQLiteHelper(activity);
                            SQLiteDatabase database = dbHelper.getWritableDatabase();
                            Search.fnDelete(search_id, database);
                            database.close();
                            refresh();
                        }
                    })
                    // nevermind
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            builder.create().show();
        }
    };
}