package com.arbol.reegle.models;

import android.database.sqlite.SQLiteDatabase;
import com.arbol.reegle.db.Reegle_Country_Table;
import com.arbol.reegle.utility.DbUtils;
import com.arbol.reegle.utility.ListUtils;

import java.util.ArrayList;

/**
 * Created by user on 1/20/14.
 */
public class Country {
    public static ArrayList<String> listAll(SQLiteDatabase database){
        return DbUtils.listAll(database, Reegle_Country_Table.TABLE_NAME, Reegle_Country_Table.COLUMN_NAME);
    }

    public static String getCodes(SQLiteDatabase database, String countryNames){
        ArrayList<String> l = DbUtils.getList(
                database,
                Reegle_Country_Table.COLUMN_CODE,
                Reegle_Country_Table.TABLE_NAME,
                Reegle_Country_Table.COLUMN_NAME,
                countryNames.split(", ")
        );
        return ListUtils.join(l, ",");
    }
}
