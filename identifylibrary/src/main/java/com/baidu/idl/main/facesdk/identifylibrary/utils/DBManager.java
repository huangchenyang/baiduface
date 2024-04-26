package com.baidu.idl.main.facesdk.identifylibrary.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public boolean isKeyDataExist(String table_name,String key,String value) {
//        List<String> dataList = new ArrayList<>();
        Cursor cursor = database.query(table_name, null, null, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex(key));
                if(name==null){
                    continue;
                }
                if(name.equals(value)){
                    cursor.close();
                    return true;
                }
            }
            cursor.close();
        }
        return false;
    }

    public void insertLineData(String table_name, HashMap<String, String> infoMap) {
        ContentValues values = new ContentValues();
        for (Map.Entry<String, String> entry : infoMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            values.put(key, value);
        }
        database.insert(table_name, null, values);
    }

    public void updateLineData(String tableName, String licid,  HashMap<String, String> infoMap) {
        ContentValues values = new ContentValues();
        for (Map.Entry<String, String> entry : infoMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            values.put(key, value);
        }
        String whereClause = "身份证号码=?";
        String[] whereArgs = new String[] { licid };
        database.update(tableName, values, whereClause, whereArgs);
    }

    public HashMap<String, String> getLineData(String tableName, String licid){
        HashMap<String, String> infoMap = new HashMap<>();
        String[] selectionArgs = {String.valueOf(licid)};
        String selection = "身份证号码=?";

        // 执行查询操作
        Cursor cursor = database.query("my_table", null, selection, selectionArgs, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            // 从游标中读取数据
            for (int i = 0; i < cursor.getColumnCount(); i++) {
                // 通过列索引获取列名，并从游标中读取该列的数据
                String columnName = cursor.getColumnName(i);
                String columnValue = cursor.getString(i);
                infoMap.put(columnName,columnValue);
            }
        }
        return infoMap;
    }
}

