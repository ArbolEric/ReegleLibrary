package com.arbol.reegle.utility;;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import com.arbol.reegle.db.Favorite_Table;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by user on 12/23/13.
 */
public class ReegleDoc {
    private static final String reegleTitle = "title";
    private static final String reegleLink = "link";
    private static final String reeglePreview = "preview";
    private static final String reegleSource = "source";
    private static final String reegleSourceId = "sourceId";
    private static final String reegleId = "id";

    public String title;
    public String preview;
    public String link;
    public String docId;// Reegle Id
    public String source;
    public String source_id;

    public ReegleDoc(Cursor mCursor) {
        ContentValues values = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(mCursor, values);
        setValues(values);
    }
    public ReegleDoc (ContentValues values){
        setValues(values);
    }


    public ReegleDoc(JSONObject json) throws JSONException {
        title = json.getString(reegleTitle);
        preview = json.getString(reeglePreview);
        link = json.getString(reegleLink);
        docId = json.getString(reegleId);
        source = json.getString(reegleSource);
        source_id = json.getString(reegleSourceId);
    }

    public ReegleDoc setValues(ContentValues values){
        title = values.getAsString(Favorite_Table.COLUMN_TITLE);
        preview = values.getAsString(Favorite_Table.COLUMN_PREVIEW);
        link = values.getAsString(Favorite_Table.COLUMN_LINK);
        docId = values.getAsString(Favorite_Table.COLUMN_DOCID);
        source = values.getAsString(Favorite_Table.COLUMN_SOURCE);
        source_id = values.getAsString(Favorite_Table.COLUMN_SOURCEID);
        return this;
    }

    public ContentValues toValues(){
        ContentValues values = new ContentValues();
        values.put(Favorite_Table.COLUMN_TITLE, title);
        values.put(Favorite_Table.COLUMN_PREVIEW, preview);
        values.put(Favorite_Table.COLUMN_LINK, link);
        values.put(Favorite_Table.COLUMN_DOCID, docId);
        values.put(Favorite_Table.COLUMN_SOURCE, source);
        values.put(Favorite_Table.COLUMN_SOURCEID, source_id);
        return values;
    }

}
