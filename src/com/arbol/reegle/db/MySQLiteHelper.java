package com.arbol.reegle.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "reegle.db";
    private static final int DATABASE_VERSION = 1;
    private static Context context;

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        Log.d("Arbol", "Creating the database");
        Search_Table.onCreate(database);
        Favorite_Table.onCreate(database);
        Read_Table.onCreate(database);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Search_Table.onUpgrade(db, oldVersion, newVersion);
        Favorite_Table.onUpgrade(db, oldVersion, newVersion);
        Read_Table.onUpgrade(db, oldVersion, newVersion);
    }


}