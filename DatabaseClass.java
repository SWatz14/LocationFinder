package com.example.assignment2;

import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class DatabaseClass extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "database.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "locations";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_LATITUDE = "latitude";

    public DatabaseClass(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ADDRESS + " TEXT, " +
                COLUMN_LONGITUDE + " REAL, " +
                COLUMN_LATITUDE + " REAL " +
                ");";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Method to insert data into the table
    public void insertData(String address, String longitude, String latitude) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ADDRESS, address);
            values.put(COLUMN_LONGITUDE, longitude);
            values.put(COLUMN_LATITUDE, latitude);
            db.insert(TABLE_NAME, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to retrieve all data (returns a Cursor)
    public Cursor getAllData() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }

    public Cursor getDataByAddress(String address) {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_ADDRESS + " = ?";
        return db.rawQuery(query, new String[]{address});
    }

    // Method to delete data from the table
    public void deleteData(String address) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            db.delete(TABLE_NAME, COLUMN_ADDRESS +
                    " = ?", new String[]{address});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to update data in the table
    public void updateData(String address, String longitude, String latitude) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_LONGITUDE, longitude);
            values.put(COLUMN_LATITUDE, latitude);
            db.update(TABLE_NAME, values, COLUMN_ADDRESS +
                    " = ?", new String[]{address});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to drop the table
    public void deleteTable() {
        try (SQLiteDatabase db = getWritableDatabase()) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

