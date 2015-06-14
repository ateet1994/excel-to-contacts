package com.ateet.excel;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "xltocontacts.db";
    public static final String TABLE_NAME = "contacts";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PHONE1 = "phone1";
    public static final String COLUMN_PHONE2 = "phone2";
    public static final String COLUMN_PHONE3 = "phone3";
    public static final String COLUMN_PHONE4 = "phone4";
    public static final String COLUMN_PHONE5 = "phone5";

    public static final String COLUMN_EMAIL1 = "email1";
    public static final String COLUMN_EMAIL2 = "email2";
    public static final String COLUMN_EMAIL3 = "email3";
    public static final String COLUMN_EMAIL4 = "email4";
    public static final String COLUMN_EMAIL5 = "email5";

    public static final int COL_ID = 0;
    public static final int COL_NAME = 1;
    public static final int COL_PHONE1 = 2;
    public static final int COL_PHONE2 = 3;
    public static final int COL_PHONE3 = 4;
    public static final int COL_PHONE4 = 5;
    public static final int COL_PHONE5 = 6;
    public static final int COL_EMAIL1 = 7;
    public static final int COL_EMAIL2 = 8;
    public static final int COL_EMAIL3 = 9;
    public static final int COL_EMAIL4 = 10;
    public static final int COL_EMAIL5 = 11;



    public static final String[] PROJECTIONS = {
            COLUMN_ID,
            COLUMN_NAME,
            COLUMN_PHONE1,
            COLUMN_PHONE2,
            COLUMN_PHONE3,
            COLUMN_PHONE4,
            COLUMN_PHONE5,
            COLUMN_EMAIL1,
            COLUMN_EMAIL2,
            COLUMN_EMAIL3,
            COLUMN_EMAIL4,
            COLUMN_EMAIL5
    };

    public static final int[] cols = {
            COL_NAME,
            COL_PHONE1,
            COL_PHONE2,
            COL_PHONE3,
            COL_PHONE4,
            COL_PHONE5,
            COL_EMAIL1,
            COL_EMAIL2,
            COL_EMAIL3,
            COL_EMAIL4,
            COL_EMAIL5
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
                COLUMN_PHONE1 + " TEXT UNIQUE NOT NULL, " +
                        COLUMN_PHONE2 + " TEXT UNIQUE, " +
                        COLUMN_PHONE3 + " TEXT UNIQUE, " +
                        COLUMN_PHONE4 + " TEXT UNIQUE, " +
                        COLUMN_PHONE5 + " TEXT UNIQUE, " +
                COLUMN_EMAIL1 + " text, " +
                        COLUMN_EMAIL2 + " text," +
                        COLUMN_EMAIL3 + " text," +
                        COLUMN_EMAIL4 + " text," +
                        COLUMN_EMAIL5 + " text" +
                        ");";
        Log.d("", SQL_CREATE_CONTACTS_TABLE);

        db.execSQL(SQL_CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
    public boolean insertContact  (String[] insert)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_NAME, insert[0]);
        contentValues.put(COLUMN_PHONE1, insert[1]);
        contentValues.put(COLUMN_PHONE2, insert[2]);
        contentValues.put(COLUMN_PHONE3, insert[3]);
        contentValues.put(COLUMN_PHONE4, insert[4]);
        contentValues.put(COLUMN_PHONE5, insert[5]);
        contentValues.put(COLUMN_EMAIL1, insert[6]);
        contentValues.put(COLUMN_EMAIL2, insert[7]);
        contentValues.put(COLUMN_EMAIL3, insert[8]);
        contentValues.put(COLUMN_EMAIL4, insert[9]);
        contentValues.put(COLUMN_EMAIL5, insert[10]);

        try {
            db.insertWithOnConflict(TABLE_NAME, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        //} catch (SQLiteConstraintException e) {

        } catch (Exception e) {
            //Toast.makeText(get, e.printStackTrace(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        return true;
    }

    public boolean updateContact (long id, String[] insert)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NAME, insert[0]);
        contentValues.put(COLUMN_PHONE1, insert[1]);
        contentValues.put(COLUMN_PHONE2, insert[2]);
        contentValues.put(COLUMN_PHONE3, insert[3]);
        contentValues.put(COLUMN_PHONE4, insert[4]);
        contentValues.put(COLUMN_PHONE5, insert[5]);
        contentValues.put(COLUMN_EMAIL1, insert[6]);
        contentValues.put(COLUMN_EMAIL2, insert[7]);
        contentValues.put(COLUMN_EMAIL3, insert[8]);
        contentValues.put(COLUMN_EMAIL4, insert[9]);
        contentValues.put(COLUMN_EMAIL5, insert[10]);
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
    public void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);

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
