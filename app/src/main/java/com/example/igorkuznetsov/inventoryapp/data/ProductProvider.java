package com.example.igorkuznetsov.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import static com.example.igorkuznetsov.inventoryapp.data.ProductContract.*;

public class ProductProvider extends ContentProvider

    {
        public static final String LOG_TAG = ProductProvider.class.getSimpleName();
        private static final int PRODUCTS = 100;
        private static final int PRODUCT_ID = 101;

        private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        static {
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_PRODUCTS, PRODUCTS);
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_PRODUCTS + "/#", PRODUCT_ID);
    }

        private ProductDbHelper mDbHelper;

        @Override
        public boolean onCreate() {

        mDbHelper = new ProductDbHelper(getContext());
        return true;
    }

        @Override
        public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(),uri);

        return cursor;
    }

        @Override
        public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }


        private Uri insertProduct(Uri uri, ContentValues values) {
        String name = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Product requires a name");
        }

        String model = values.getAsString(ProductEntry.COLUMN_PRODUCT_MODEL);
        if(model==null) {
            throw new IllegalArgumentException("Product  requires a model ");
        }
            Double price = values.getAsDouble(ProductEntry.COLUMN_PRODUCT_PRICE);
            if(price==0 || price<0) {
                throw new IllegalArgumentException("Product have problem witch price");
            }
         Integer quantity = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            if(quantity < 0) {
                throw new IllegalArgumentException("Quantity must be >= 0");
            }
         String supplierName = values.getAsString(ProductEntry.COLUMN_SUPPLIER_EMAIL);
         String supplierEmail = values.getAsString(ProductEntry.COLUMN_SUPPLIER_EMAIL);
            if(supplierName == null || supplierEmail == null){
                throw new IllegalArgumentException("The product must have a suppler and his email address");
            }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        long newRowId = db.insert(ProductEntry.TABLE_NAME, null, values);

        if (newRowId == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri,null);
        return ContentUris.withAppendedId(uri, newRowId);
    }


        @Override
        public int update(Uri uri, ContentValues contentValues, String selection,
                          String[] selectionArgs) {
            final int match = sUriMatcher.match(uri);
            switch (match) {
                case PRODUCTS:
                    return updateProduct(uri, contentValues, selection, selectionArgs);
                case PRODUCT_ID:
                    // For the PRODUCT_ID code, extract out the ID from the URI,
                    // so we know which row to update. Selection will be "_id=?" and selection
                    // arguments will be a String array containing the actual ID.
                    selection = ProductContract.ProductEntry._ID + "=?";
                    selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                    return updateProduct(uri, contentValues, selection, selectionArgs);
                default:
                    throw new IllegalArgumentException("Update is not supported for " + uri);
            }

        }

        /**
         * Update products in the database with the given content values. Apply the changes to the rows
         * specified in the selection and selection arguments (which could be 0 or 1 or more products).
         * Return the number of rows that were successfully updated.
         */
        private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

            if (values.containsKey(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME)) {
                String name = values.getAsString(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
                if (name == null) {
                    throw new IllegalArgumentException("Product requires a name");
                }
            }


            if (values.containsKey(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY)) {
                Integer quantity = values.getAsInteger(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
                if (quantity != null && quantity < 0) {
                    throw new IllegalArgumentException("Product requires valid quantity");
                }
            }

            if (values.size() == 0) {
                return 0;
            }

            SQLiteDatabase database = mDbHelper.getWritableDatabase();

            int rowsUpdated = database.update(ProductContract.ProductEntry.TABLE_NAME, values, selection, selectionArgs);

            if (rowsUpdated != 0) {
                getContext().getContentResolver().notifyChange(uri, null);
            }

            return rowsUpdated;

        }
        @Override
        public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsDeleted;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                rowsDeleted = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:

                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                getContext().getContentResolver().notifyChange(uri,null);
                rowsDeleted = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

        @Override
        public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
    }
