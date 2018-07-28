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

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.pets.data.PetContract;
import com.example.android.pets.data.PetContract.PetEntry;
import com.example.android.pets.data.PetDBHelper;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity {

    private final int zero = 0;
    private int INTENT_MODE = zero;
    private final int INTENT_ADD = 0;
    private final int INTENT_UPDATE = 1;
    private final int INTENT_DELETE = 2;

    private EditText mIDEditText;
    private EditText mNameEditText;
    private EditText mBreedEditText;
    private EditText mWeightEditText;
    private Spinner mGenderSpinner;

    private boolean mGenderSelected = false;
    private int mGenderSwitch = zero;
    private int mGender = zero;
    private int badID = -1;

    private PetDBHelper mDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        mIDEditText = (EditText) findViewById(R.id.edit_pet_id);
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);
        mDBHelper = new PetDBHelper(this);
        fulfillExtras();
        setupSpinner();
    }

    private void fulfillExtras() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            switch (bundle.getString(PetContract.INTENT_EXTRA)) {
                case PetContract.INTENT_UPDATE:                                                             // Update pet
                    INTENT_MODE = INTENT_UPDATE;
                    setUI(View.VISIBLE,
                            View.VISIBLE,
                            View.VISIBLE,
                            View.VISIBLE,
                            View.VISIBLE);
                    this.setTitle(getApplicationContext().getResources().getString(R.string.action_update_pet));
                    break;
                default:                                                                                    // Add pet
                    INTENT_MODE = INTENT_ADD;
                    setUI(View.GONE,
                            View.VISIBLE,
                            View.VISIBLE,
                            View.VISIBLE,
                            View.VISIBLE);
                    break;
            }
        } else {
            INTENT_MODE = INTENT_ADD;
            setUI(View.GONE,
                    View.VISIBLE,
                    View.VISIBLE,
                    View.VISIBLE,
                    View.VISIBLE);
        }
    }

    private void setUI(int visibility_1, int visibility_2, int visibility_3, int visibility_4, int visibility_5) {
        mIDEditText.setVisibility(visibility_1);
        mNameEditText.setVisibility(visibility_2);
        mBreedEditText.setVisibility(visibility_3);
        mWeightEditText.setVisibility(visibility_4);
        mGenderSpinner.setVisibility(visibility_5);
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

            // Because AdapterView is an abstract class, onNothingSelected must be defined
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

    private boolean checkAttributes(String value) {
        if (!TextUtils.isEmpty(value) && !value.equals(null)) {
            return true;
        }
        return false;
    }

    private void updatePet() {
        ContentValues values = new ContentValues();
        long id = Long.valueOf(mIDEditText.getText().toString().trim());
        implementContentValues(values);
        Uri uriID = ContentUris.withAppendedId(PetEntry.CONTENT_URI, id);
        String selection = PetEntry._ID + "=?";
        String[] selectionArgs = { String.valueOf(ContentUris.withAppendedId(PetEntry.CONTENT_URI, id)) };
        int newRowID = getContentResolver().update(uriID, values, selection, selectionArgs);
    }

    private void implementContentValues(ContentValues values) {
        if (checkAttributes(mNameEditText.getText().toString().trim())) {
            String name = mNameEditText.getText().toString().trim();
            values.put(PetEntry.COLUMN_PET_NAME, name);
        }
        if (checkAttributes(mBreedEditText.getText().toString().trim())) {
            String breed = mBreedEditText.getText().toString().trim();
            values.put(PetEntry.COLUMN_PET_BREED, breed);
        }
        if (checkAttributes(mWeightEditText.getText().toString().trim())) {
            int weight = Integer.parseInt(mWeightEditText.getText().toString());
            values.put(PetEntry.COLUMN_PET_WEIGHT, weight);
        }
        if (mGenderSelected) {
            values.put(PetEntry.COLUMN_PET_GENDER, mGender);
        }
    }

    private void deletePet() {
        String idString = mIDEditText.getText().toString().trim();
        String nameString = mNameEditText.getText().toString().trim();
        String breedString = mBreedEditText.getText().toString().trim();
        int weightInt = Integer.valueOf(mWeightEditText.getText().toString().trim());
        if (mGenderSelected) {
            int genderInt = mGender;
        }
        if (isEmpty(idString)) {       // is empty...
//            Toast.makeText(this, getApplicationContext().getResources().getString(R.string.error_input_provide_id), Toast.LENGTH_SHORT).show();

        } else {
            String selection = PetEntry._ID + "=?";
            long id = Long.valueOf(idString);
            String[] selectionArgs = new String[] {String.valueOf(ContentUris.withAppendedId(PetEntry.CONTENT_URI, id))};
            Uri uriID = ContentUris.withAppendedId(PetEntry.CONTENT_URI, id);
            int newRowID = getContentResolver().delete(uriID, selection, selectionArgs);
            if (newRowID > zero) {
                Toast.makeText(this, "Pet data deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Nothing deleted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isEmpty(String value) { return TextUtils.isEmpty(value); }

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
                    finish();
                }
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}