package com.sysu.sdcs.weatherstation;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class UserInfoDB {
    SQLiteDatabase db;

    public UserInfoDB(Context context) {
        DBOpenHandler dbOpenHandler = new DBOpenHandler(context, "dbUserInfo.db3", null, 1);
        db = dbOpenHandler.getWritableDatabase();
    }

    public void insert(String tableName, ContentValues cv) {
        db.insert(tableName, null, cv);
    }

    public int update(String tableName, ContentValues cv, String where, String[] whereArgs) {
        return db.update(tableName, cv, where, whereArgs);
    }

    public int delete(String tableName, String where, String[] whereArgs) {
        return db.delete(tableName, where, whereArgs);
    }

    public Cursor query(String tableName, String[] projection, String where, String[] whereArgs, String sortOrder) {
        return db.query(tableName, projection, where, whereArgs, null, null, sortOrder, null);
    }
}
