package com.arbol.reegle.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.arbol.reegle.db.Favorite_Table;
import com.arbol.reegle.utility.DbUtils;
import com.arbol.reegle.utility.ReegleDoc;

import java.util.ArrayList;

/**
 * This database class has a doc field that uses the ReegleDoc class
 * to handle Reegle document objects.
 */
public class Favorite {

    /*
     * Class Attributes and Methods
     */
    static public ArrayList<ReegleDoc> all(SQLiteDatabase database) {
        Cursor mCursor = database.query(Favorite_Table.TABLE_FAVORITES, null, null, null, null, null, Favorite_Table.COLUMN_TITLE);
        mCursor.moveToFirst();
        ArrayList<ReegleDoc> aValues = new ArrayList<ReegleDoc>();
        while (!mCursor.isAfterLast()){
            ReegleDoc reegleDoc = new ReegleDoc(mCursor);
            aValues.add(reegleDoc);
            mCursor.moveToNext();
        }
        mCursor.close();
        return aValues;
    }

    // Save new Search to database
    static public Long fnInsert(ContentValues values, SQLiteDatabase database) {
        // DOCID, SOURCEID, SOURCE, LINK, TITLE, PREVIEW
        return database.insert(Favorite_Table.TABLE_FAVORITES, Favorite_Table.COLUMNS.toString(), values);
    }

    static public ArrayList<String> fnCheck(ArrayList<ReegleDoc> aDocs, SQLiteDatabase database){
        return DbUtils.checkIdList(aDocs, Favorite_Table.TABLE_FAVORITES, Favorite_Table.COLUMN_DOCID, database);
    }

    static public boolean fnDelete(String docId, SQLiteDatabase database){
        String where = String.format("%s=?",
                Favorite_Table.COLUMN_DOCID
        );
        int i = database.delete(Favorite_Table.TABLE_FAVORITES, where, new String[]{docId});
        if (i == 0){
            return false;
        }
        return true;
    }

    static public boolean toggle(ReegleDoc doc, SQLiteDatabase database){
        // returns true if document added, false if document removed from favorites.
        String sql = String.format("SELECT count(*) FROM %s WHERE %s=?;",
                Favorite_Table.TABLE_FAVORITES, Favorite_Table.COLUMN_DOCID
                );
        Cursor cursor = database.rawQuery(sql, new String[]{doc.docId});
        cursor.moveToFirst();
        if (cursor.getInt(0) == 0){
            cursor.close();
            fnInsert(doc.toValues(), database);
            return true;
        } else {
            cursor.close();
            fnDelete(doc.docId, database);
            return false;
        }
    }

    /*
     * Instance Attributes
     */

    public Long id;
    public ReegleDoc doc;

    /*
     * Public Constructors (Instances)
     */

    // Retrieving search from database
    public Favorite(Cursor cursor) {
        this.id = cursor.getLong(cursor.getColumnIndex(Favorite_Table.COLUMN_ID));
        doc = new ReegleDoc(cursor);
    }

    public Favorite(String docId, SQLiteDatabase database) {
        Cursor mCursor = database.query(Favorite_Table.TABLE_FAVORITES, null,
                Favorite_Table.COLUMN_DOCID +"=?",
                new String[]{docId},
                null, null, null
        );
        mCursor.moveToFirst();
        new Favorite(mCursor);
        mCursor.close();
    }

    // Save changes to database
    public void update(SQLiteDatabase database) {
        String where = Favorite_Table.COLUMN_ID + "=?";
        database.update(Favorite_Table.TABLE_FAVORITES, doc.toValues(), where, new String[]{Long.toString(id)});
    }
    public void delete(SQLiteDatabase database){
        fnDelete(doc.docId, database);
    }
}
