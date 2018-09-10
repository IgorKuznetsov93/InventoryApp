package com.example.igorkuznetsov.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.igorkuznetsov.inventoryapp.data.ProductContract.ProductEntry;

public class ProductDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Products.db";

    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String SQL_CREATE_PETS_TABLE = "CREATE TABLE " + ProductEntry.TABLE_NAME +" ("
                + ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + ProductEntry.COLUMN_PRODUCT_MODEL + " TEXT NOT NULL, "
                + ProductEntry.COLUMN_PRODUCT_PICTURE + " TEXT NOT NULL, "
                + ProductEntry.COLUMN_PRODUCT_PRICE + " REAL NOT NULL, "
                + ProductEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER DEFAULT 0, "
                + ProductEntry.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL, "
                + ProductEntry.COLUMN_SUPPLIER_EMAIL + " TEXT NOT NULL);";
        sqLiteDatabase.execSQL(SQL_CREATE_PETS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}