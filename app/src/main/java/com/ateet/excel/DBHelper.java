package com.ateet.excel;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "xltocontacts.db";
    public static final String TABLE_NAME = "contacts";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_EMAIL = "email";

    public static final int COL_ID = 0;
    public static final int COL_NAME = 1;
    public static final int COL_PHONE = 2;
    public static final int COL_EMAIL = 3;

    public static final String[] PROJECTIONS = {
            COLUMN_ID,
            COLUMN_NAME,
            COLUMN_PHONE,
            COLUMN_EMAIL
    };

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_CONTACTS_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_NAME + " TEXT NOT NULL, " +
                COLUMN_PHONE + " TEXT UNIQUE NOT NULL, " +
                COLUMN_EMAIL + " text);";

        db.execSQL(SQL_CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
    public boolean insertContact  (String name, String phone, String email)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_NAME, name);
        contentValues.put(COLUMN_PHONE, phone);
        contentValues.put(COLUMN_EMAIL, email);

        try {
            db.insertWithOnConflict(TABLE_NAME, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        //} catch (SQLiteConstraintException e) {

        } catch (Exception e) {
            //Toast.makeText(get, e.printStackTrace(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        return true;
    }

    public boolean updateContact (long id, String name, String phone, String email)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NAME, name);
        contentValues.put(COLUMN_PHONE, phone);
        contentValues.put(COLUMN_EMAIL, email);
        db.update(TABLE_NAME, contentValues, COLUMN_ID + " = ? ", new String[]{Long.toString(id)});
        return true;
    }
    public Cursor getData(long id){
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery( "select * from " + TABLE_NAME + " where " + COLUMN_ID + " = "+ id + "", null );
    }
    public Integer deleteContact (long id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME,
                COLUMN_ID + " = ? ",
                new String[]{Long.toString(id)});
    }
    public Cursor getAllContacts()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String sortOrder = COLUMN_NAME + " ASC";
        Cursor res =  db.query(TABLE_NAME, PROJECTIONS, null, null, null, null, sortOrder);
        res.moveToFirst();
        return res;
    }

    public void bulkInsert(ContentValues[] values) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            for (ContentValues value : values) {
//                try {
                    db.insertWithOnConflict(TABLE_NAME, null, value, SQLiteDatabase.CONFLICT_REPLACE);
                    //} catch (SQLiteConstraintException e) {
                    //Toast.makeText(get, e.printStackTrace(), Toast.LENGTH_LONG).show();
                    //e.printStackTrace();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

}
