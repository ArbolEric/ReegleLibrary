package com.arbol.reegle.models;

import android.database.sqlite.SQLiteDatabase;
import com.arbol.reegle.utility.DbUtils;
import com.arbol.reegle.db.Reegle_Topic_Table;
import com.arbol.reegle.utility.ListUtils;

import java.util.ArrayList;

/**
 * Created by user on 1/20/14.
 */
public class Topic {
    public static ArrayList<String> listAll(SQLiteDatabase database){
        return DbUtils.listAll(database, Reegle_Topic_Table.TABLE_NAME, Reegle_Topic_Table.COLUMN_NAME);
    }
    public static String getCodes(SQLiteDatabase database, String topicNames){
        ArrayList<String> l = DbUtils.getList(
                database,
                Reegle_Topic_Table.COLUMN_URL,
                Reegle_Topic_Table.TABLE_NAME,
                Reegle_Topic_Table.COLUMN_NAME,
                topicNames.split(", ")
        );
        return ListUtils.join(l, ",");
    }

}
