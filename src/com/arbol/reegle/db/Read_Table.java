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
public class Read_Table {
    public static final String TABLE_READ = "read";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DOCID = "docId";
    public static final String[] COLUMNS = {

    };

    // Database creation SQL statement
    private static final String CREATE = String.format(
            "create table %s (%s %s, %s %s);",
            TABLE_READ,
            COLUMN_ID, "integer primary key autoincrement",
            COLUMN_DOCID, "text not null"
    );

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(Read_Table.class.getName(), "Upgrading database from version "
                + oldVersion + " to " + newVersion);
    }

    // Mark new doc as read
    public String fnAdd(String docId){
        String INSERT = String.format(
                "INSERT INTO %s (%s) VALUES (%s);",
                Read_Table.TABLE_READ, Read_Table.COLUMN_DOCID, docId
        );
        return INSERT;
    }

    // Mark new doc as read
    public String fnDelete(Integer id){
        String DELETE = String.format(
                "DELETE FROM %s WHERE %s='%s';",
                Read_Table.TABLE_READ, Read_Table.COLUMN_ID, id.toString()
        );
        return DELETE;
    }

    // Check
}