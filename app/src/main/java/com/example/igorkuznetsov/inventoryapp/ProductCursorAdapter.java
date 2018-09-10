package com.example.igorkuznetsov.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.igorkuznetsov.inventoryapp.data.ProductContract;

public class ProductCursorAdapter extends CursorAdapter {

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.content_inventory_main, viewGroup,false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {


        TextView nameTextView = (TextView) view.findViewById(R.id.product_name);
        TextView modelTextView = (TextView) view.findViewById(R.id.product_model);
        TextView priceTextView = (TextView) view.findViewById(R.id.product_price);
        TextView quantityTextView = (TextView) view.findViewById(R.id.product_current_quantity);
        ImageView photoImageView = (ImageView) view.findViewById(R.id.product_image);
        ImageButton buyImageButton = (ImageButton) view.findViewById(R.id.product_buy_button);


        final int productIdColumnIndex = cursor.getInt(cursor.getColumnIndex(ProductContract.ProductEntry._ID));
        int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
        int modelColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_MODEL);
        int priceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int photoColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PICTURE);


        String productName = cursor.getString(nameColumnIndex);
        String productModel = cursor.getString(modelColumnIndex);
        String productPrice = cursor.getString(priceColumnIndex);
        final int quantityProduct = cursor.getInt(quantityColumnIndex);
        String imageUriString = cursor.getString(photoColumnIndex);
        Uri productImageUri = Uri.parse(imageUriString);


        nameTextView.setText(productName);
        modelTextView.setText(productModel);
        priceTextView.setText(productPrice);
        quantityTextView.setText(String.valueOf(quantityProduct));
        photoImageView.setImageURI(productImageUri);

        buyImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri productUri = ContentUris.withAppendedId(ProductContract.ProductEntry.CONTENT_URI, productIdColumnIndex);
                adjustProductQuantity(context, productUri, quantityProduct);
            }
        });
    }


    private void adjustProductQuantity(Context context, Uri productUri, int currentQuantityInStock) {

        int newQuantityValue = (currentQuantityInStock >= 1) ? currentQuantityInStock - 1 : 0;

        if (currentQuantityInStock == 0) {
            Toast.makeText(context.getApplicationContext(), R.string.toast_out_of_stock_msg, Toast.LENGTH_SHORT).show();
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, newQuantityValue);
        int numRowsUpdated = context.getContentResolver().update(productUri, contentValues, null, null);


    }
}