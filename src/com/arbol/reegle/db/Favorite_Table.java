package com.arbol.reegle.db;

import org.json.JSONObject;
import java.net.URL;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created with IntelliJ IDEA.
 * User: user
 * Date: 11/21/13
 * Time: 6:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class Favorite_Table {
    public static final String TABLE_FAVORITES = "favorites";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DOCID = "docId";
    public static final String COLUMN_SOURCEID = "sourceId";
    public static final String COLUMN_SOURCE = "source";
    public static final String COLUMN_LINK = "link";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_PREVIEW = "preview";
    public static final String[] COLUMNS = {
        COLUMN_DOCID, COLUMN_SOURCEID, COLUMN_SOURCE, COLUMN_LINK,
            COLUMN_TITLE, COLUMN_PREVIEW
    };

    // Database creation SQL statement
    public static final String CREATE = String.format(
            "create table %s (%s %s, %s %s, %s %s, %s %s, %s %s, %s %s, %s %s);",
            TABLE_FAVORITES,
            COLUMN_ID, "integer primary key autoincrement",
            COLUMN_DOCID, " text not null",
            COLUMN_SOURCEID , "text",
            COLUMN_SOURCE, "text",
            COLUMN_LINK, "text not null",
            COLUMN_TITLE, "text not null",
            COLUMN_PREVIEW, "text"
    );

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(Favorite_Table.class.getName(), "Upgrading database from version "
                + oldVersion + " to " + newVersion);
    }
}