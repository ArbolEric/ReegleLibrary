package com.arbol.reegle.db;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created with IntelliJ IDEA.
 * User: user
 * Date: 11/21/13
 * Time: 6:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class Search_Table {
    public static final String COLUMN_ID = "_id";
    public static final String TABLE_SEARCH = "search";
    public static final String COLUMN_DISPLAY = "name";
    public static final String COLUMN_LANGUAGES = "languages";
    public static final String COLUMN_TOPICS = "topics";
    //public static final String COLUMN_SOURCES = "sources";
    public static final String COLUMN_COUNTRIES = "countries";
    public static final String[] COLUMNS = {
        COLUMN_DISPLAY, COLUMN_LANGUAGES, COLUMN_TOPICS, COLUMN_COUNTRIES
    };

    // Database creation SQL statement
    public static final String CREATE = String.format(
            "create table %s (%s %s, %s %s, %s %s, %s %s, %s %s);",
            TABLE_SEARCH,
            COLUMN_ID, "integer primary key autoincrement",
            COLUMN_DISPLAY, "text",
            COLUMN_LANGUAGES, "text",
            COLUMN_TOPICS, "text",
            COLUMN_COUNTRIES, "text"
    );


    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(Search_Table.class.getName(), "Upgrading database from version "
                + oldVersion + " to " + newVersion);
    }
}
