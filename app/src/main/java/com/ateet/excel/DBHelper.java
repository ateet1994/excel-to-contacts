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
    public static final String COLUMN_ID = "_ID";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_EMAIL = "email";

    public static final int COL_NAME = 0;
    public static final int COL_PHONE = 1;
    public static final int COL_EMAIL = 2;

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

    public boolean updateContact (Integer id, String name, String phone, String email)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NAME, name);
        contentValues.put(COLUMN_PHONE, phone);
        contentValues.put(COLUMN_EMAIL, email);
        db.update(TABLE_NAME, contentValues, COLUMN_ID +" = ? ", new String[]{Integer.toString(id)});
        return true;
    }
    public Cursor getData(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery( "select * from " + TABLE_NAME + " where " + COLUMN_ID + " = "+ id + "", null );
    }
    public Integer deleteContact (int id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME,
                COLUMN_ID + " = ? ",
                new String[]{Integer.toString(id)});
    }
    public void getAllContacts(ArrayList array_list, ArrayList<Integer> listId)
    {
        array_list.clear();
        listId.clear();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("select * from " + TABLE_NAME, null);
        res.moveToFirst();
        while(!res.isAfterLast()){
            array_list.add(res.getString(res.getColumnIndex(COLUMN_NAME)));
            listId.add(res.getInt(res.getColumnIndex(COLUMN_ID)));
            res.moveToNext();
        }
        res.close();
    }
    public void searchKeyString(String key, ArrayList<String> array_list, ArrayList<Integer> listId){
        array_list.clear();
        listId.clear();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + COLUMN_NAME + " LIKE ?";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery,  new String[] {key+"%"});
        // db.rawQuery("SELECT * FROM "+table+" WHERE KEY_KEY LIKE ?", new String[] {key+"%"});
        // if you want to get everything starting with that key value
        if (cursor.moveToFirst()) {
            do {
                array_list.add(cursor.getString(1));
                listId.add(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

}
