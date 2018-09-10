package com.example.igorkuznetsov.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.igorkuznetsov.inventoryapp.data.ProductContract;
import com.example.igorkuznetsov.inventoryapp.data.ProductContract.ProductEntry;


public class InventoryMain extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    ProductCursorAdapter mAdapter;

    private static final int PRODUCT_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_main);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Intent intent = new Intent(InventoryMain.this, EditorActivity.class);
               startActivity(intent);
            }
        });

        ListView listView =  findViewById(R.id.list_view_product);

        View emptyView = findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);


        mAdapter = new ProductCursorAdapter(this,null);

        listView.setAdapter(mAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(InventoryMain.this, EditorActivity.class);
                Uri currentPetUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI,id);
                intent.setData(currentPetUri);
                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_inventory_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_delete_all_data:
                deleteAllProducts();
                return true;
            case R.id.action_insert_dummy_data:
                insertProduct();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }


    private void insertProduct() {



        // Get Uri for example photo from drawable resource
        Uri imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getResources().getResourcePackageName(R.drawable.example)
                + '/' + getResources().getResourceTypeName(R.drawable.example) + '/' + getResources().getResourceEntryName(R.drawable.example));

        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, "Sony");
        values.put(ProductEntry.COLUMN_PRODUCT_MODEL, "xca");
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, 7);
        values.put(ProductEntry.COLUMN_PRODUCT_PICTURE, String.valueOf(imageUri));
        values.put(ProductEntry.COLUMN_SUPPLIER_EMAIL, "supplier@mail.ru");
        values.put(ProductEntry.COLUMN_SUPPLIER_NAME, "supplier");
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, 49.99);

        // Insert a new row for Toto into the provider using the ContentResolver.
        // Use the {@link ProductEntry#CONTENT_URI} to indicate that we want to insert
        // into the products database table.
        // Receive the new content URI that will allow us to access Toto's data in the future.
        Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
    }

    private void deleteAllProducts() {
        int rowsDeleted = getContentResolver().delete(ProductContract.ProductEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from pet database");

    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_MODEL,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_PICTURE };
        return new CursorLoader(this,ProductEntry.CONTENT_URI,projection,null,null,null);
        }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
