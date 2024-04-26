package com.baidu.idl.main.facesdk.identifylibrary.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

public class DBManager {
    private DBHelper dbHelper;
    private SQLiteDatabase database;

    public DBManager(Context context) {
        dbHelper = new DBHelper(context);
        database = dbHelper.getWritableDatabase();
    }

    public void insertData(String table_name,String key, String name) {
        ContentValues values = new ContentValues();
        values.put(key, name);
        database.insert(table_name, null, values);
    }

    public List<String> getAllData(String table_name,String key) {
        List<String> dataList = new ArrayList<>();
        Cursor cursor = database.query(table_name, null, null, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex(key));
                dataList.add(name);
            }
            cursor.close();
        }
        return dataList;
    }

    public void deleteTable(String tableName) {
        database.execSQL("DROP TABLE IF EXISTS " + tableName);
    }
}

