package com.arbol.reegle.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.arbol.reegle.MainActivity;
import com.arbol.reegle.db.MySQLiteHelper;
import com.arbol.reegle.models.Favorite;
import com.arbol.reegle.models.Read;
import com.arbol.reegle.R;
import com.arbol.reegle.utility.ReegleDoc;

import java.util.ArrayList;

/**
 * Created by user on 12/21/13.
 */
public class DocumentAdapter extends BaseAdapter {
    public ArrayList<ReegleDoc> docs;
    private LayoutInflater inflater;
    private MySQLiteHelper dbHelper;
    private ArrayList<String> favoriteIds;
    private ArrayList<String> readIds;
    private Boolean bFavorites;
    private MainActivity activity;

    /*
     * Constructors
     */

    public DocumentAdapter(MainActivity activity) {
        // by default rend results from favorites
        super();
        setContext(activity);
    }

    public void setContext(MainActivity activity){
        this.activity = activity;
        this.dbHelper = new MySQLiteHelper(activity);
        this.inflater = (LayoutInflater) activity.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
    }


    private void setDocs(ArrayList<ReegleDoc> reegleDocs){
        Log.d("Arbol", "documentAdapter.setDocs");
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        if (docs == null) {
            this.docs = reegleDocs;
        } else {
            Log.d("Arbol", "data set changing");
            this.docs = reegleDocs;
            Log.d("Arbol", Integer.toString(reegleDocs.size()));
            notifyDataSetChanged();
            Log.d("Arbol", "data set changed");
        }
        favoriteIds = Favorite.fnCheck(docs, database);
        readIds = Read.fnCheck(docs, database);
        database.close();
    }

    public void addRead(String docId){
        if (!readIds.contains(docId)){
            readIds.add(docId);
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            Read.fnInsert(docId, database);
            database.close();
        }
    }

    public void showFavorites(SQLiteDatabase database){
        bFavorites = true;
        setDocs(Favorite.all(database));
    }

    public void setResults(ArrayList<ReegleDoc> reegleDocs) {
        if (reegleDocs == null){
            Log.d("Arbol", "reegleDocs null");
            reegleDocs = new ArrayList<ReegleDoc>();
        }
        Log.d("Arbol", "documentAdapter.setResults");
        bFavorites = false;
        setDocs(reegleDocs);
    }

    @Override
    public boolean isEmpty(){
        if (docs.size() == 0){
            return true;
        }
        return false;
    }

    @Override
    public int getCount() {
        return docs.size();
    }

    @Override
    public ReegleDoc getItem(int position) {
        return docs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View groupView = convertView;
        DocHolder docHolder;
        if (groupView == null) {
            groupView = inflater.inflate(R.layout.doc_item, null);
            docHolder = new DocHolder();
            docHolder.name = (TextView) groupView.findViewById(R.id.document_name);
            docHolder.preview = (TextView) groupView.findViewById(R.id.document_preview);
            docHolder.favorite = (ImageView) groupView.findViewById(R.id.favorite_document);
            groupView.setTag(docHolder);
        } else {
            docHolder = (DocHolder) groupView.getTag();
        }
        ReegleDoc doc = docs.get(position);
        docHolder.name.setText(doc.title);
        docHolder.preview.setText(doc.preview);
        docHolder.name.setTag(doc);
        docHolder.favorite.setTag(doc);
        /*
         * Display document text style and favorite star based on status
         */

        if (readIds.contains(doc.docId)){
            // If document read
            docHolder.name.setTypeface(Typeface.DEFAULT);
        } else {
            docHolder.name.setTypeface(Typeface.DEFAULT_BOLD);
        }
        if (bFavorites){
            // all favorited and all read
            docHolder.favorite.setImageResource(R.drawable.ic_action_unstar);
        } else {
            if (favoriteIds.contains(doc.docId)){
                // If document already favorited
                docHolder.favorite.setImageResource(R.drawable.ic_action_unstar);
            } else {
                docHolder.favorite.setImageResource(R.drawable.ic_action_star);
            }
        }

        // Toggle documents in and out of Favorites table
        docHolder.name.setOnClickListener(openDocument());
        docHolder.favorite.setOnClickListener(toggleFavoritesListener);
        return groupView;
    }

    /*
     * View Holders
     */

    static class DocHolder {
        public TextView name;
        public TextView preview;
        public ImageView favorite;
    }

    /*
     * Click Listeners
     */

    private final View.OnClickListener toggleFavoritesListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ReegleDoc doc = (ReegleDoc) v.getTag();
            if (bFavorites) { // If this is a list of favorites
                buildDeleteFavDialog(doc, activity);
            } else {
                SQLiteDatabase database = dbHelper.getWritableDatabase();
                Boolean bAdded = Favorite.toggle(doc, database);
                database.close();
                if (bAdded){
                    ((ImageView) v).setImageResource(R.drawable.ic_action_unstar);
                    favoriteIds.add(doc.docId);
                } else {
                    ((ImageView) v).setImageResource(R.drawable.ic_action_star);
                    favoriteIds.remove(favoriteIds.indexOf(doc.docId));
                }
            }

        }
    };

    private void buildDeleteFavDialog(ReegleDoc doc, Context context){
        final String docId = doc.docId;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.confirm_remove_favorite)
                // confirmed
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // first confirm that they want to delete the search
                        SQLiteDatabase database = dbHelper.getWritableDatabase();
                        Favorite.fnDelete(docId, database);
                        showFavorites(database);
                        database.close();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        builder.create().show();
    }

    private View.OnClickListener openDocument(){

        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ReegleDoc doc = (ReegleDoc) view.getTag();
                addRead(doc.docId);
                ((TextView) view.findViewById(R.id.document_name)).setTypeface(Typeface.DEFAULT);
                Uri uri = Uri.parse(doc.link);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                activity.startActivity(intent);
            }
        };
    }
}