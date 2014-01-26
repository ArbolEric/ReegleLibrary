package com.arbol.reegle.models;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import com.arbol.reegle.db.Read_Table;
import com.arbol.reegle.utility.ReegleDoc;
import com.arbol.reegle.utility.DbUtils;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: user
 * Date: 11/21/13
 * Time: 8:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class Read {
    /*
     * Class Attributes and Methods
     */
    static public ArrayList<String> fnCheck(ArrayList<ReegleDoc> aDocs, SQLiteDatabase database){
        return DbUtils.checkIdList(aDocs, Read_Table.TABLE_READ, Read_Table.COLUMN_DOCID, database);
    }

    static public void fnInsert(String docId, SQLiteDatabase database){
        ContentValues values = new ContentValues();
        values.put(Read_Table.COLUMN_DOCID, docId);
        database.insert(Read_Table.TABLE_READ, Read_Table.COLUMN_DOCID, values);
    }

}
