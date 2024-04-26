package com.baidu.idl.main.facesdk.identifylibrary.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "mydatabase.db";
    private static final int DATABASE_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // 创建表
        String createTableQuery = "CREATE TABLE IF NOT EXISTS my_table ("
                + "ID TEXT,"
                + "设备型号 TEXT,"
                + "设备编号 TEXT,"
                + "核验时间 TEXT,"
                + "核验结果 TEXT,"
                + "核验类型 TEXT,"
                + "证件类别 TEXT,"
                + "姓名 TEXT,"
                + "性别 TEXT,"
                + "民族或国籍 TEXT,"
                + "出生日期 TEXT,"
                + "地址 TEXT,"
                + "身份证号码 TEXT,"
                + "签发机关 TEXT,"
                + "证件有效期起 TEXT,"
                + "证件有效期止 TEXT,"
                + "英文姓名 TEXT,"
                + "证件版本号 TEXT,"
                + "签发次数 TEXT,"
                + "通行证号码 TEXT,"
                + "证件照片 TEXT,"
                + "现场照片 TEXT,"
                + "FS TEXT,"
                + "MS TEXT,"
                + "卡号 TEXT,"
                + "办理业务 TEXT,"
                + "联系方式 TEXT,"
                + "备注 TEXT,"
                + "FACEYUZHI TEXT,"
                + "FINGERYUZHI TEXT,"
                + "FINGERFS TEXT,"
                + "FINGER  TEXT)";
        db.execSQL(createTableQuery);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 升级数据库
        db.execSQL("DROP TABLE IF EXISTS my_table");
        onCreate(db);
    }
}

