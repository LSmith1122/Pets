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
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.pets.data.PetContract;
import com.example.android.pets.data.PetContract.PetEntry;
import com.example.android.pets.data.PetDBHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private final int zero = 0;
    private int INTENT_MODE = zero;
    private final int INTENT_ADD = 0;
    private final int INTENT_UPDATE = 1;
    private final int INTENT_DELETE = 2;
    private final int ASYNC_LOADER_ID = 0;
    private final int CURSOR_LOADER_ID = 1;

    private EditText mNameEditText;
    private EditText mBreedEditText;
    private EditText mWeightEditText;
    private Spinner mGenderSpinner;

    private String nameString;
    private String breedString;
    private String weightString;
    private String appendage = "=?";

    private boolean mPetHasChanged = false;
    private boolean mGenderSelected = false;
    private int mGenderSwitch = zero;
    private int mGender = zero;
    private int badID = -1;
    private int genderInt = badID;

    private PetDBHelper mDBHelper;

    private Intent intent;
    private Bundle bundle;
    private Uri selectedPetURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        initIntentData();
        initViews();
        setupSpinner();
        fulfillExtras();
    }

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mPetHasChanged = true;
            return false;
        }
    };

    private void initIntentData() {
        mDBHelper = new PetDBHelper(this);
        intent = getIntent();
        bundle = intent.getExtras();
        selectedPetURI = intent.getData();
    }

    private void initViews() {
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);
        mNameEditText.setOnTouchListener(mTouchListener);
        mBreedEditText.setOnTouchListener(mTouchListener);
        mWeightEditText.setOnTouchListener(mTouchListener);
    }

    private void fulfillExtras() {
        if (bundle != null) {
            try {
                switch (bundle.getString(PetContract.INTENT_EXTRA)) {
                    case PetContract.INTENT_UPDATE:                                                             // Update pet
                        INTENT_MODE = INTENT_UPDATE;
                        if (isEditingSinglePet()) {
                            LoaderManager loaderManager = getLoaderManager();
                            loaderManager.initLoader(CURSOR_LOADER_ID, null, this).forceLoad();
                        }
                        this.setTitle(getApplicationContext().getResources().getString(R.string.action_update_pet));
                        break;
                    default:                                                                                    // Add pet
                        INTENT_MODE = INTENT_ADD;
                        break;
                }
            } catch (NullPointerException e) {
                Log.e("Error", "Issue with intent bundle or extras", e);
            }
        } else {
            INTENT_MODE = INTENT_ADD;
        }
    }

    private boolean isEditingSinglePet() { return bundle.containsKey(PetEntry._ID) && selectedPetURI != null; }

    private void displayInfoForSelectedPet(Cursor cursor) {
        if (cursor != null) {
            cursor.moveToNext();
            int id = Integer.parseInt(String.valueOf(ContentUris.parseId(selectedPetURI)));
            int nameColumn = cursor.getColumnIndexOrThrow(PetEntry.COLUMN_PET_NAME);
            int breedColumn = cursor.getColumnIndexOrThrow(PetEntry.COLUMN_PET_BREED);
            int weightColumn = cursor.getColumnIndexOrThrow(PetEntry.COLUMN_PET_WEIGHT);
            int genderColumn = cursor.getColumnIndexOrThrow(PetEntry.COLUMN_PET_GENDER);
            mNameEditText.setText(cursor.getString(nameColumn));            // Name String
            mBreedEditText.setText(cursor.getString(breedColumn));           // Breed String
            mWeightEditText.setText(String.valueOf(cursor.getInt(weightColumn)));          // Weight Integer
            mGenderSpinner.setSelection(cursor.getInt(genderColumn));      // Gender Integer
            cursor.close();
        }
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mPetHasChanged = true;
                    }
                });
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetEntry.GENDER_MALE; // Male
                        if (mGenderSwitch > 0) {
                            mGenderSelected = true;
                        }
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetEntry.GENDER_FEMALE; // Female
                        if (mGenderSwitch > 0) {
                            mGenderSelected = true;
                        }
                    } else {
                        mGender = PetEntry.GENDER_UNKNOWN; // Unknown
                        if (mGenderSwitch > 0) {
                            mGenderSelected = true;
                        }
                    }
                    mGenderSwitch = 1;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0; // Unknown
                mGenderSelected = false;
            }
        });
    }

    private void insertPet() {
        ContentValues values = new ContentValues();
        String name = mNameEditText.getText().toString().trim();
        String breed = mBreedEditText.getText().toString().trim();
        int gender = mGender;
        int weight = Integer.parseInt(mWeightEditText.getText().toString());
        values.put(PetEntry.COLUMN_PET_NAME, name);
        values.put(PetEntry.COLUMN_PET_BREED, breed);
        values.put(PetEntry.COLUMN_PET_GENDER, gender);
        values.put(PetEntry.COLUMN_PET_WEIGHT, weight);
        values.put(PetEntry.COLUMN_PET_GENDER, gender);
        Uri uri = getContentResolver().insert(PetEntry.CONTENT_URI, values);
    }

    private void updatePet() {
        int newRowID = badID;
        ContentValues values = new ContentValues();
        implementContentValues(values);
        if (isEditingSinglePet()) {
            newRowID = getContentResolver().update(selectedPetURI, values, null, null);
        } else {
            throw new NullPointerException("Error: Possible bad operation or null / invalid URI");
        }
    }

    private void implementContentValues(ContentValues values) {
        if (!isEmpty(mNameEditText.getText().toString().trim())) {
            String name = mNameEditText.getText().toString().trim();
            values.put(PetEntry.COLUMN_PET_NAME, name);
        }
        if (!isEmpty(mBreedEditText.getText().toString().trim())) {
            String breed = mBreedEditText.getText().toString().trim();
            values.put(PetEntry.COLUMN_PET_BREED, breed);
        }
        if (!isEmpty(mWeightEditText.getText().toString().trim())) {
            int weight = Integer.parseInt(mWeightEditText.getText().toString());
            values.put(PetEntry.COLUMN_PET_WEIGHT, weight);
        }
        if (mGenderSelected) {
            values.put(PetEntry.COLUMN_PET_GENDER, mGender);
        }
    }

    private void deletePet() {
        if (isValidEntryForDeletion()) {
            DialogInterface.OnClickListener deleteButtonClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    continuePetDeletion();
                    finish();
                }
            };
            deletePetDialog(deleteButtonClickListener);
        } else {
            Toast.makeText(this, getApplicationContext().getResources().getString(R.string.error_input_provide_attributes_for_deletion), Toast.LENGTH_LONG).show();
        }
    }

    private void continuePetDeletion() {
        genderInt = badID;
        recordAttributes();
        if (mGenderSelected) {
            genderInt = mGender;
        }
        Uri uriID = null;
        String selection = null;
        String[] selectionArgs = null;
        try {
            if (isEditingSinglePet()) {
                uriID = selectedPetURI;
            } else {
                if (!isEmpty(nameString) || !isEmpty(breedString) || !isEmpty(weightString) || genderInt != badID) {               // if ID Entry is provided...
                    retrieveSelectionAndSelectionArgs(selection, selectionArgs);
                    uriID = PetEntry.CONTENT_URI;
                }
            }
            int newRowID = getContentResolver().delete(uriID, selection, selectionArgs);
            if (newRowID > zero) {
                Toast.makeText(this, getApplicationContext().getResources().getString(R.string.update_pet_data_deleted), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getApplicationContext().getResources().getString(R.string.update_nothing_deleted), Toast.LENGTH_SHORT).show();
            }
        } catch (IllegalArgumentException e) {
            Log.e(getApplicationContext().toString(), "Error adding attributes or selecting row ID for deletion", e);
        }
    }

    private void retrieveSelectionAndSelectionArgs(String selection, String[] selectionArgs) {
        List<String> columnList = new ArrayList<>();
        List<String> inputList = new ArrayList<>();
        if (!isEmpty(nameString)) {
            columnList.add(PetEntry.COLUMN_PET_NAME);
            inputList.add(nameString);
        }
        if (!isEmpty(breedString)) {
            columnList.add(PetEntry.COLUMN_PET_BREED);
            inputList.add(breedString);
        }
        if (!isEmpty(weightString)) {
            columnList.add(PetEntry.COLUMN_PET_WEIGHT);
            inputList.add(weightString);
        }
        if (genderInt != badID) {
            columnList.add(PetEntry.COLUMN_PET_GENDER);
            inputList.add(String.valueOf(genderInt));
        }
        // retrieves the 1st & only item in returned list - the method converted all items in provided list into a single selection String
        selection = (addSecondaryAttributes(appendage, columnList.toArray(new String[0])))[0];
        selectionArgs = addSecondaryAttributes(null, inputList.toArray(new String[0]));
    }

    private void recordAttributes() {
        nameString = mNameEditText.getText().toString().trim();
        breedString = mBreedEditText.getText().toString().trim();
        weightString = mWeightEditText.getText().toString().trim();
    }

    private String[] addSecondaryAttributes(String s, String[] valueList) {
        List<String> list = new ArrayList<>();
        if (s == null) {                            // to use with selectionArgs ONLY
            list = compileSecondaryAttributes(null, valueList);
            return list.toArray(new String[0]);
        } else if (s.equals(appendage)) {                                    // to use with selection ONLY
            list = compileSecondaryAttributes(s, valueList);
            StringBuilder builder = new StringBuilder();
            String a = ", ";
            for (int i = 0; i < list.size(); i++) {
                int lastPos = list.size() - 1;
                String ending = "";
                if (i < lastPos) {
                    ending = a;
                }
                String value = list.get(i) + ending;
                builder.append(value);
            }
            return new String[] { builder.toString() };
        } else {
            throw new IllegalArgumentException("Incorrect appendage statement while adding secondary attributes");
        }
    }

    private List<String> compileSecondaryAttributes(String s, String[] valueList) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < valueList.length; i++) {
            if (s == null) {        // for selectionArgs String[] ONLY
                s = "";
                if (!isEmpty(valueList[i])) {
                    String value = valueList[i] + s;
                    list.add(value);
                }
            } else if (s.equals(appendage)) {                // for selection String ONLY
                switch (valueList[i]) {
                    case PetEntry.COLUMN_PET_NAME:
                        addValuesToList(list, valueList[i], s);
                        break;
                    case PetEntry.COLUMN_PET_BREED:
                        addValuesToList(list, valueList[i], s);
                        break;
                    case PetEntry.COLUMN_PET_WEIGHT:
                        addValuesToList(list, valueList[i], s);
                        break;
                    case PetEntry.COLUMN_PET_GENDER:
                        addValuesToList(list, valueList[i], s);
                        break;
                }
            }
        }
        return list;
    }

    private void addValuesToList(List<String> list, String string, String s) {
        String value = string + s;
        list.add(value);
    }

    private boolean isEmpty(String value) { return TextUtils.isEmpty(value); }

    private boolean isValidEntryForDeletion() {
        return (!isEmpty(nameString) || !isEmpty(breedString) || !isEmpty(weightString) || mGenderSelected);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        MenuItem deleteOption = (MenuItem) menu.findItem(R.id.action_delete);
        if (INTENT_MODE == INTENT_ADD ) {
            deleteOption.setVisible(false);
        } else {
            deleteOption.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                try {
                    switch (INTENT_MODE) {
                        case INTENT_ADD:
                            insertPet();
                            break;
                        case INTENT_UPDATE:
                            updatePet();
                            break;
                        case INTENT_DELETE:
                            break;
                        default:            // revert back to INTENT_ADD
                            break;
                    }
                } catch (Exception e) {
                    Log.e("EditorActivity", "Error inserting pet data", e);
                    Toast.makeText(this, "Error with saving pet.\nPlease fill in all data.", Toast.LENGTH_SHORT).show();
                } finally {
                    finish();
                }
                return true;
            case R.id.action_delete:
                if (INTENT_MODE != INTENT_ADD) {
                    deletePet();
                }
                return true;
            case android.R.id.home:
                if (!mPetHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
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

    private void deletePetDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_selected_pet_dialog);
        builder.setPositiveButton(R.string.delete_pet, discardButtonClickListener);
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

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
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
        if (!mPetHasChanged) {              // if no changes were made, go back...
            super.onBackPressed();
            return;
        }
        // otherwise, if changes were made...
        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void clearInputFields() {
        mNameEditText.setText(null);
        mBreedEditText.setText(null);
        mWeightEditText.setText(null);
        mGenderSpinner.setSelection(PetEntry.GENDER_UNKNOWN);
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
                return new CursorLoader(getApplicationContext(), selectedPetURI, projection, null, null, null);
            case ASYNC_LOADER_ID:
                return null;
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        displayInfoForSelectedPet(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        clearInputFields();
    }
}