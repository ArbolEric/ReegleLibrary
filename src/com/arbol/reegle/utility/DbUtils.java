package com.arbol.reegle.utility;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * This class serves as a wrapper over Reegle documents for
 *   1) JSON Objects from search results
 *   2) documents stored as Favorites in the database
 */
public class DbUtils {
    static public ArrayList<String> checkIdList(ArrayList<ReegleDoc> aDocs, String table, String COLUMN_DOCID, SQLiteDatabase database){
        String docIds = "(";
        Boolean firstDoc = true;
        for (ReegleDoc doc : aDocs){
            if (firstDoc) {
                firstDoc = false;
            } else { docIds += ","; }
            docIds += "'" + doc.docId + "'";
        }
        docIds += ")";
        String where = String.format("%s IN %s",
                COLUMN_DOCID, docIds
        );
        Cursor mCursor = database.query(table, new String[]{COLUMN_DOCID}, where,
                null, null, null, null
        );
        ArrayList<String> favoriteIds = new ArrayList<String>();
        while (mCursor.moveToNext()){
            favoriteIds.add(mCursor.getString(0));

        }
        return favoriteIds;
    }
    static public ArrayList<String> getList(SQLiteDatabase database, String getColumn, String table,
                                String whereColumn, String[] inArray){
        String where = String.format("%s IN (%s)", whereColumn, "'"+ListUtils.join(inArray, "','")+"'");
        Cursor cursor = database.query(table, new String[]{getColumn}, where, null, null, null, getColumn);
        ArrayList<String> a2 = new ArrayList<String>();
        Integer i = cursor.getColumnIndex(getColumn);
        while (cursor.moveToNext()){
            a2.add(cursor.getString(i));
        }
        cursor.close();
        return a2;
    }

    static public ArrayList<String> listAll(SQLiteDatabase database, String table, String column){
        ArrayList<String> a = new ArrayList<String>();
        Cursor cursor = database.query(table, new String[]{column}, null, null, null, null, column);
        cursor.moveToFirst();
        Integer i = cursor.getColumnIndex(column);
        while (!cursor.isAfterLast()){
            a.add(cursor.getString(i));
            cursor.moveToNext();
        }
        cursor.close();
        return a;
    }
}
