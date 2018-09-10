package com.example.igorkuznetsov.inventoryapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.igorkuznetsov.inventoryapp.data.ProductContract.ProductEntry;
import com.example.igorkuznetsov.inventoryapp.data.ProductContract;


public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PRODUCT_LOADER = 0;

    private boolean hasAllRequiredValues = false;

    private Uri mImageUri;

    private int mQuantity;

    private Uri mCurrentProductUri;

    private EditText mNameEditText;

    private EditText mModelEditText;

    private ImageView mPhoto;

    private EditText mPrice;

    private EditText mSupplierName;

    private EditText mSupplierEmail;

    private EditText mQuantityEditText;

    private TextView mPhotoHintText;

    private Button mAddProductButton;

    private Button mRejectProductButton;

    private boolean mProductHasChanged = false;

    private int mCurrentQuantity = 0;


    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mModelEditText = (EditText) findViewById(R.id.edit_product_model);
        mQuantityEditText = (EditText) findViewById(R.id.edit_product_quantity);
        mPhoto = (ImageView) findViewById(R.id.edit_product_photo);
        mPrice = (EditText) findViewById(R.id.edit_product_price);
        mSupplierName = (EditText) findViewById(R.id.edit_product_supplier_name);
        mSupplierEmail = (EditText) findViewById(R.id.edit_product_supplier_email);
        mAddProductButton = (Button) findViewById(R.id.addProductButton);
        mRejectProductButton = (Button) findViewById(R.id.rejectProductButton);
        mPhotoHintText = (TextView) findViewById(R.id.add_or_edit_photo_hint);

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        if (mCurrentProductUri == null) {

            setTitle(getString(R.string.editor_activity_title_new_product));
            mPhotoHintText.setText(getText(R.string.add_photo_hint_text));
            mSupplierName.setEnabled(true);
            mSupplierEmail.setEnabled(true);
            mQuantityEditText.setEnabled(true);
            mPhoto.setImageResource(R.drawable.icon_camera);
            mRejectProductButton.setVisibility(View.GONE);

            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_product));
            mPhotoHintText.setText(getText(R.string.edit_photo_hint_text));
            mSupplierName.setEnabled(false);
            mSupplierEmail.setEnabled(false);
            mQuantityEditText.setEnabled(false);
            mAddProductButton.setVisibility(View.VISIBLE);
            mRejectProductButton.setVisibility(View.VISIBLE);

            getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
        }

        mPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trySelector();
                mProductHasChanged = true;
            }
        });


    }

    public void trySelector() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            return;
        }
        openSelector();
    }

    private void openSelector() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType(getString(R.string.intent_type));
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)), 0);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openSelector();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                mImageUri = data.getData();
                mPhoto.setImageURI(mImageUri);
                mPhoto.invalidate();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editor_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_save:
                insertProduct();
                finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case R.id.action_order_more:
                orderMore();
                return true;
            case android.R.id.home:
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {

        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    private void showDeleteConfirmationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {
        if (mCurrentProductUri != null) {

            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        finish();
    }

    private boolean insertProduct() {
        int quantity;

        String nameString = mNameEditText.getText().toString().trim();
        String modelString = mModelEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String priceString = mPrice.getText().toString().trim();
        String supplierNameString = mSupplierName.getText().toString().trim();
        String supplierEmailString = mSupplierEmail.getText().toString().trim();

        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(nameString) &&
                TextUtils.isEmpty(modelString) &&
                TextUtils.isEmpty(quantityString) &&
                TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(supplierNameString) &&
                TextUtils.isEmpty(supplierEmailString) &&
                mImageUri == null) {
            hasAllRequiredValues = true;
            return hasAllRequiredValues;
        }

        ContentValues values = new ContentValues();


        if (TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, getString(R.string.validation_msg_product_name), Toast.LENGTH_SHORT).show();
            return hasAllRequiredValues;
        } else {
            values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        }

        if (TextUtils.isEmpty(quantityString)) {
            Toast.makeText(this, getString(R.string.validation_msg_product_quantity), Toast.LENGTH_SHORT).show();
            return hasAllRequiredValues;
        } else {
            quantity = Integer.parseInt(quantityString);
            values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        }

        if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, getString(R.string.validation_msg_product_price), Toast.LENGTH_SHORT).show();
            return hasAllRequiredValues;
        } else {
            values.put(ProductEntry.COLUMN_PRODUCT_PRICE, priceString);
        }

        if (mImageUri == null) {
            Toast.makeText(this, getString(R.string.validation_msg_product_image), Toast.LENGTH_SHORT).show();
            return hasAllRequiredValues;
        } else {
            values.put(ProductEntry.COLUMN_PRODUCT_PICTURE, mImageUri.toString());
        }

        values.put(ProductEntry.COLUMN_PRODUCT_MODEL, modelString);             // optional, nullable
        values.put(ProductEntry.COLUMN_SUPPLIER_NAME, supplierNameString);      // optional, nullable
        values.put(ProductEntry.COLUMN_SUPPLIER_EMAIL, supplierEmailString);    // optional, nullable


        if (mCurrentProductUri == null) {

            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

            if (newUri == null) {

                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {

                Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {

            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);
            if (rowsAffected == 0) {

                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {

                Toast.makeText(this, getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_SHORT).show();
            }

        }

        hasAllRequiredValues = true;
        return hasAllRequiredValues;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_MODEL,
                ProductEntry.COLUMN_PRODUCT_PICTURE,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_SUPPLIER_NAME,
                ProductEntry.COLUMN_SUPPLIER_EMAIL,
                ProductEntry.COLUMN_PRODUCT_QUANTITY
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,       // Parent activity context
                mCurrentProductUri,         // Query the content URI for the current product
                projection,                 // Columns to include in the resulting Cursor
                null,                       // No selection clause
                null,                       // No selection arguments
                null);                      // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int modelColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_MODEL);
            int pictureColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PICTURE);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int supplierNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_NAME);
            int supplierEmailColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_EMAIL);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);

            String name = cursor.getString(nameColumnIndex);
            String model = cursor.getString(modelColumnIndex);
            String imageUriString = cursor.getString(pictureColumnIndex);
            String price = cursor.getString(priceColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierEmail = cursor.getString(supplierEmailColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            mQuantity = quantity;
            mImageUri = Uri.parse(imageUriString);

            mNameEditText.setText(name);
            mModelEditText.setText(model);
            mPhoto.setImageURI(mImageUri);
            mPrice.setText(price);
            mSupplierName.setText(supplierName);
            mSupplierEmail.setText(supplierEmail);
            mQuantityEditText.setText(Integer.toString(quantity));
    }}


    @Override
    public void onLoaderReset(Loader<Cursor> loader)
        {
        mNameEditText.setText("");
        mModelEditText.setText("");
        mPhoto.setImageResource(R.drawable.icon_camera);
        mPrice.setText("");
        mSupplierName.setText("");
        mSupplierEmail.setText("");
        mQuantityEditText.setText("");
    }

    public void orderMore() {
        Intent intent = new Intent(android.content.Intent.ACTION_SENDTO);
        intent.setType("text/plain");
        intent.setData(Uri.parse("mailto:" + mSupplierEmail.getText().toString().trim()));
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "New order: " +
                mNameEditText.getText().toString().trim() +
                " " + mModelEditText.getText().toString().trim());
        String message = "We need to make a new order of: " +
                mNameEditText.getText().toString().trim() +
                " " +
                mModelEditText.getText().toString().trim() + "." +
                "\n" +
                "Please confirm that you can send to us ___ pcs." +
                "\n" +
                "\n" +
                "Best regards," + "\n" +
                "_________________";
        intent.putExtra(android.content.Intent.EXTRA_TEXT, message);
        startActivity(intent);
    }
}