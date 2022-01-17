/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android.pets.data.PetContract.PetEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the pet data loader */
    private static final int PET_LOADER = 0;

    /** Adapter for the ListView */
    PetCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the pet data
        ListView petListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        petListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of pet data in the Cursor.
        // There is no pet data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new PetCursorAdapter(this, null);
        petListView.setAdapter(mCursorAdapter);


        // Setup the item click listener
        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                // Form the content URI that represents the specific pet that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link PetEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.android.pets/pets/2"
                // if the pet with ID 2 was clicked on.
                Uri currentPetUri = ContentUris.withAppendedId(PetEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentPetUri);

                // Launch the {@link EditorActivity} to display the data for the current pet.
                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(PET_LOADER, null, this);
    }

    /**
     * Helper method to insert hardcoded pet data into the database. For debugging purposes only.
     */
    private final void insertPet() {
        // Create a ContentValues object where column names are the keys,
        // and Toto's pet attributes are the values.
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, "Toto");
        values.put(PetEntry.COLUMN_PET_BREED, "Terrier");
        values.put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE);
        values.put(PetEntry.COLUMN_PET_WEIGHT, 7);

        // Insert a new row for Toto into the provider using the ContentResolver.
        // Use the {@link PetEntry#CONTENT_URI} to indicate that we want to insert
        // into the pets database table.
        // Receive the new content URI that will allow us to access Toto's data in the future.
        Uri newUri = getContentResolver().insert(PetEntry.CONTENT_URI, values);
    }

    /**
     * Helper method to delete all pets in the database.
     */
    private void deleteAllPets() {
        int rowsDeleted = getContentResolver().delete(PetEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from pet database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertPet();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllPets();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                PetEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link PetCursorAdapter} with this new cursor containing updated pet data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}
//
//import android.content.ContentUris;
//import android.content.ContentValues;
//import android.content.Intent;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//import android.net.Uri;
//import android.os.Bundle;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import android.widget.AdapterView;
//import android.widget.CursorAdapter;
//import android.widget.ListView;
//import android.widget.Toast;
//import com.example.android.pets.PetCursorAdapter;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.loader.app.LoaderManager;
//import androidx.loader.content.Loader;
//
//import com.example.android.pets.data.PetContract.PetEntry;
//import com.example.android.pets.data.PetDbHelper;
//import com.google.android.material.floatingactionbutton.FloatingActionButton;
//
///**import com.example.android.pets.PetCursorAdapter
// * Displays list of pets that were entered and stored in the app.
// */
//public class CatalogActivity extends AppCompatActivity implements
//        LoaderManager.LoaderCallbacks<Cursor> {
//
//    /** Identifier for the pet data loader */
//    private static final int PET_LOADER = 0;
//
//    /** Adapter for the ListView */
//    PetCursorAdapter mCursorAdapter;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_catalog);
//
//        // Setup FAB to open EditorActivity
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        // Find the ListView which will be populated with the pet data
//        ListView petListView = (ListView) findViewById(R.id.list);
//
//        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
//        View emptyView = findViewById(R.id.empty_view);
//        petListView.setEmptyView(emptyView);
//
//        // Setup an Adapter to create a list item for each row of pet data in the Cursor.
//        // There is no pet data yet (until the loader finishes) so pass in null for the Cursor.
//        mCursorAdapter = new PetCursorAdapter(this, null);
//        petListView.setAdapter(mCursorAdapter);
//
//        // Setup the item click listener
//        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
//                // Create new intent to go to {@link EditorActivity}
//                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
//
//                // Form the content URI that represents the specific pet that was clicked on,
//                // by appending the "id" (passed as input to this method) onto the
//                // {@link PetEntry#CONTENT_URI}.
//                // For example, the URI would be "content://com.example.android.pets/pets/2"
//                // if the pet with ID 2 was clicked on.
//                Uri currentPetUri = ContentUris.withAppendedId(PetEntry.CONTENT_URI, id);
//
//                // Set the URI on the data field of the intent
//                intent.setData(currentPetUri);
//
//                // Launch the {@link EditorActivity} to display the data for the current pet.
//                startActivity(intent);
//            }
//        });
//
//        // Kick off the loader
//        getLoaderManager().initLoader(PET_LOADER, null, this);
//    }
//
//  */
//    private void insertPet() {
//        // Create a ContentValues object where column names are the keys,
//        // and Toto's pet attributes are the values.
//        ContentValues values = new ContentValues();
//        values.put(PetEntry.COLUMN_PET_NAME, "Toto");
//        values.put(PetEntry.COLUMN_PET_BREED, "Terrier");
//        values.put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE);
//        values.put(PetEntry.COLUMN_PET_WEIGHT, 7);
//
//        // Insert a new row for Toto into the provider using the ContentResolver.
//        // Use the {@link PetEntry#CONTENT_URI} to indicate that we want to insert
//        // into the pets database table.
//        // Receive the new content URI that will allow us to access Toto's data in the future.
//        Uri newUri = getContentResolver().insert(PetEntry.CONTENT_URI, values);
//    }
//    @Override
//    protected void onStart() {
//        super.onStart();
//        displayDatabaseInfo();
//
//    }
//
//    /**
//     * Temporary helper method to display information in the onscreen TextView about the state of
//     * the pets database.
//     */
//    private void displayDatabaseInfo() {
//        // Create and/or open a database to read from it
//       // SQLiteDatabase db = mDbHelper.getReadableDatabase();
//
//        // Define a projection that specifies which columns from the database
//        // you will actually use after this query.
//        String[] projection = {
//                PetEntry._ID,
//                PetEntry.COLUMN_PET_NAME,
//                PetEntry.COLUMN_PET_BREED,
//                PetEntry.COLUMN_PET_GENDER,
//                PetEntry.COLUMN_PET_WEIGHT };
//
//        // Perform a query on the pets table
////        Cursor cursor = db.query(
////                PetEntry.TABLE_NAME,   // The table to query
////                projection,            // The columns to return
////                null,                  // The columns for the WHERE clause
////                null,                  // The values for the WHERE clause
////                null,                  // Don't group the rows
////                null,                  // Don't filter by row groups
////                null);                   // The sort order
//        Cursor cursor=getContentResolver().query(PetEntry.CONTENT_URI,projection,null,null,null,null);
//        madapter =new PetCursorAdapter(this,cursor);
////        TextView displayView = (TextView) findViewById(R.id.text_view_pet);
//        ListView petListView = (ListView) findViewById(R.id.list);
//        //try {
//            // Create a header in the Text View that looks like this:
//            //
//            // The pets table contains <number of rows in Cursor> pets.
//            // _id - name - breed - gender - weight
//            //
//            // In the while loop below, iterate through the rows of the cursor and display
//            // the information from each column in this order.
////            displayView.setText("The pets table contains " + cursor.getCount() + " pets.\n\n");
////            displayView.append(PetEntry._ID + " - " +
////                    PetEntry.COLUMN_PET_NAME + " - " +
////                    PetEntry.COLUMN_PET_BREED + " - " +
////                    PetEntry.COLUMN_PET_GENDER + " - " +
////                    PetEntry.COLUMN_PET_WEIGHT + "\n");
//            // Setup an Adapter to create a list item for each row of pet data in the Cursor.
//
//
//            // Figure out the index of each column
////            int idColumnIndex = cursor.getColumnIndex(PetEntry._ID);
////            int nameColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME);
////            int breedColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED);
////            int genderColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER);
////            int weightColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT);
////
////            // Iterate through all the returned rows in the cursor
////            while (cursor.moveToNext()) {
////                // Use that index to extract the String or Int value of the word
////                // at the current row the cursor is on.
////                int currentID = cursor.getInt(idColumnIndex);
////                String currentName = cursor.getString(nameColumnIndex);
////                String currentBreed = cursor.getString(breedColumnIndex);
////                int currentGender = cursor.getInt(genderColumnIndex);
////                int currentWeight = cursor.getInt(weightColumnIndex);
////                // Display the values from each column of the current row in the cursor in the TextView
////                displayView.append(("\n" + currentID + " - " +
////                        currentName + " - " +
////                        currentBreed + " - " +
////                        currentGender + " - " +
////                        currentWeight));
////            }
////        } finally {
////            // Always close the cursor when you're done reading from it. This releases all its
////            // resources and makes it invalid.
////            cursor.close();
////        }
//            petListView.setAdapter(adapter);
//           // cursor.close();
//    }
//
//    /**
//     * Helper method to insert hardcoded pet data into the database. For debugging purposes only.
//     */
////    private void insertPet() {
////        // Gets the database in write mode
////        //SQLiteDatabase db = mDbHelper.getWritableDatabase();
////
////        // Create a ContentValues object where column names are the keys,
////        // and Toto's pet attributes are the values.
////        ContentValues values = new ContentValues();
////        values.put(PetEntry.COLUMN_PET_NAME, "Toto");
////        values.put(PetEntry.COLUMN_PET_BREED, "Terrier");
////        values.put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE);
////        values.put(PetEntry.COLUMN_PET_WEIGHT, 7);
////
////        // Insert a new row for Toto in the database, returning the ID of that new row.
////        // The first argument for db.insert() is the pets table name.
////        // The second argument provides the name of a column in which the framework
////        // can insert NULL in the event that the ContentValues is empty (if
////        // this is set to "null", then the framework will not insert a row when
////        // there are no values).
////        // The third argument is the ContentValues object containing the info for Toto.
////        //long newRowId = db.insert(PetEntry.TABLE_NAME, null, values);
////        Uri newUri = getContentResolver().insert(PetEntry.CONTENT_URI, values);
////
////        // Show a toast message depending on whether or not the insertion was successful
////        if (newUri == null) {
////            // If the new content URI is null, then there was an error with insertion.
////            Toast.makeText(this, getString(R.string.editor_insert_pet_failed),
////                    Toast.LENGTH_SHORT).show();
////        } else {
////            // Otherwise, the insertion was successful and we can display a toast.
////            Toast.makeText(this, getString(R.string.editor_insert_pet_successful),
////                    Toast.LENGTH_SHORT).show();
////        }
////    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu options from the res/menu/menu_catalog.xml file.
//        // This adds menu items to the app bar.
//        getMenuInflater().inflate(R.menu.menu_catalog, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // User clicked on a menu option in the app bar overflow menu
//        switch (item.getItemId()) {
//            // Respond to a click on the "Insert dummy data" menu option
//            case R.id.action_insert_dummy_data:
//                insertPet();
//                displayDatabaseInfo();
//                return true;
//            // Respond to a click on the "Delete all entries" menu option
//            case R.id.action_delete_all_entries:
//                // Do nothing for now
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    @NonNull
//    @Override
//    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
//
//        return null;
//    }
//
//    @Override
//    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
//
//    }
//
//    @Override
//    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
//
//    }
//}
