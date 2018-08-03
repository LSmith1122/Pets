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

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.android.pets.data.PetContract;
import com.example.android.pets.data.PetContract.PetEntry;
import com.example.android.pets.data.PetDBHelper;
import com.example.android.pets.data.PetProvider;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private final String LOG_TAG = PetProvider.class.getSimpleName();
    private PetDBHelper mDBHelper;
    private final int zero = 0;
    private final int ASYNC_LOADER_ID = 0;
    private final int CURSOR_LOADER_ID = 1;
    private ListView listView;
    private RelativeLayout emptyViewGroup;
    private PetCursorAdapter cursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);
        mDBHelper = new PetDBHelper(this);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        listView = (ListView) findViewById(R.id.list);
        emptyViewGroup = (RelativeLayout) findViewById(R.id.empty_view);
        cursorAdapter = new PetCursorAdapter(this, null);
        listView.setAdapter(cursorAdapter);
        listView.setEmptyView(emptyViewGroup);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), EditorActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(PetEntry._ID, String.valueOf(position));
                bundle.putString(PetContract.INTENT_EXTRA, PetContract.INTENT_UPDATE);
                intent.putExtras(bundle);
                Uri selectedURI = ContentUris.withAppendedId(PetEntry.CONTENT_URI, id);
                intent.setData(selectedURI);
                startActivity(intent);
            }
        });
        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(CURSOR_LOADER_ID, null, this).forceLoad();
    }

    private void insertDummyPetData() {
        ContentValues newValues = new ContentValues();
        newValues.put(PetEntry.COLUMN_PET_NAME, "Tommy");
        newValues.put(PetEntry.COLUMN_PET_BREED, "Terrier");
        newValues.put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE);
        newValues.put(PetEntry.COLUMN_PET_WEIGHT, 16);
        Uri uri = getContentResolver().insert(PetEntry.CONTENT_URI, newValues);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
        Bundle bundle;
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                insertDummyPetData();
                return true;
            case R.id.action_delete_all_entries:
                DialogInterface.OnClickListener deletePetDialogOnClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int deleteRows = getContentResolver().delete(PetEntry.CONTENT_URI, null, null);
                        if (deleteRows > zero) {
                            Toast.makeText(getApplicationContext(), "All data deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Error deleting data", Toast.LENGTH_SHORT).show();
                        }
                    }
                };
                deletePetDialog(deletePetDialogOnClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
        switch (loaderID) {
            case CURSOR_LOADER_ID:
                String[] projection = {
                        PetEntry._ID,
                        PetEntry.COLUMN_PET_NAME,
                        PetEntry.COLUMN_PET_BREED,
                        PetEntry.COLUMN_PET_WEIGHT,
                        PetEntry.COLUMN_PET_GENDER};
                return new CursorLoader(this, PetEntry.CONTENT_URI, projection, null, null, null);
            case ASYNC_LOADER_ID:
                return null;
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        cursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }

    private void deletePetDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_pet_dialog);
        builder.setPositiveButton(R.string.delete_all_pets, discardButtonClickListener);
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
}