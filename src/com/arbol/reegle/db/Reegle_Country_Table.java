package com.arbol.reegle.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by user on 1/20/14.
 */
public class Reegle_Country_Table {
    public static final String COLUMN_ID = "_id";
    public static final String TABLE_NAME = "countries";
    public static final String COLUMN_CODE = "code";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_NAME = "name";
    public static final String[] COUNTRIES_COLUMNS = {
            COLUMN_URL, COLUMN_NAME, COLUMN_CODE
    };

    // Database creation SQL statement
    public static final String CREATE =
            String.format("CREATE TABLE %s (%s %s, %s %s, %s %s, %s %s);",
                    TABLE_NAME, COLUMN_ID, "integer primary key autoincrement", COLUMN_URL, "text",
                    COLUMN_NAME, "text", COLUMN_CODE, "text"
            );

    public static void onCreate(SQLiteDatabase database, Context context) {
        // do nothing
    }
}
